package space.maxus.macrocosm.npc.shop

import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.banking.Transaction
import space.maxus.macrocosm.players.banking.transact
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.removeAnySlot
import kotlin.math.ceil
import kotlin.math.roundToInt
import space.maxus.macrocosm.util.containsAtLeast as containsLeast

private fun itemAsPurchaseHistory(item: ItemStack, model: MacrocosmItem = item.macrocosm!!): ItemStack {
    val price = model.sellPrice
    item.meta {
        displayName(displayName()!!.append(text(" <dark_gray>x${item.amount}")))

        val lore = lore()
        lore!!.addAll(
            listOf(
                "",
                "<gray>Cost",
                "<gold>${Formatting.withCommas(price.toFloat().toBigDecimal())} Coins",
                "",
                "<yellow>Click to buyback!"
            ).map { text(it).noitalic() }
        )
        lore(lore)
    }
    return item
}

private fun modifyItemMeta(next: ItemStack, mc: MacrocosmItem) {
    next.meta {
        val lore = lore()!!.toMutableList()

        lore.addAll(listOf(
            "",
            "<gray>Sell Price",
            "<gold>${Formatting.withCommas(mc.sellPrice.toFloat().toBigDecimal())} Coins",
            "",
            "<yellow>Click to sell!"
        ).map { text(it).noitalic() })

        lore(lore)

        displayName(displayName()!!.append(Component.text(" x${next.amount}").color(NamedTextColor.DARK_GRAY)))
    }
}

private fun GUIBuilder<ForInventorySixByNine>.setupListeners(player: MacrocosmPlayer) {
    val listener = listen<InventoryClickEvent> { e ->
        if (
            e.whoClicked.uniqueId != player.ref ||
            e.currentItem.isAirOrNull() ||
            e.clickedInventory != e.view.bottomInventory
        )
            return@listen
        val mc = e.currentItem!!.macrocosm!!
        if (mc is Unsellable)
            return@listen
        val coins = mc.sellPrice.toFloat().toBigDecimal()
        val copy = mc.build(player)!!
        player.shopHistory.lastSold.add(copy)
        if (player.shopHistory.lastSold.size >= player.shopHistory.limit)
            player.shopHistory.lastSold.removeFirst()
        player.purse += transact(coins, player.ref, Transaction.Kind.INCOMING)
        e.view.bottomInventory.clear(e.slot)
        e.view.topInventory.setItem(
            Slots.RowOneSlotFive.inventorySlot.realSlotIn(GUIType.SIX_BY_NINE.dimensions)!!,
            itemAsPurchaseHistory(copy.clone(), mc)
        )

        sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
            pitch = 2f
            volume = 2f
            playFor(e.whoClicked as Player)
        }
    }

    val p = player.paper!!
    val iter = p.inventory.iterator()
    while (iter.hasNext()) {
        val next: ItemStack = iter.next() ?: continue
        val mc = next.macrocosm!!
        modifyItemMeta(next, mc)
        iter.set(next)
    }

    onClose {
        listener.unregister()

        val thisIter = p.inventory.iterator()
        while (thisIter.hasNext()) {
            val next = thisIter.next() ?: continue
            val mc = next.macrocosm!!
            thisIter.set(mc.build(player))
        }
    }
}

fun shopUi(player: MacrocosmPlayer, model: ShopModel): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text(model.name)

    setupListeners(player)

    page(0) {
        placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE))

        button(
            Slots.RowOneSlotFive, if (player.shopHistory.lastSold.isEmpty()) ItemValue.placeholderDescripted(
                Material.HOPPER,
                "<green>Sell Item",
                "Click items in your inventory to",
                "sell them to this Shop!"
            ) else itemAsPurchaseHistory(player.shopHistory.lastSold.last().clone())
        ) { e ->
            e.bukkitEvent.isCancelled = true
            if (player.shopHistory.lastSold.isEmpty())
                return@button
            val lastMod = player.shopHistory.lastSold.removeLast()
            val mc = lastMod.macrocosm!!
            val last = mc.build(player)!!
            val price = mc.sellPrice.toFloat().toBigDecimal()
            if (player.purse < price)
                return@button
            player.purse -= transact(price, player.ref, Transaction.Kind.OUTGOING)
            modifyItemMeta(last, mc)
            e.player.giveOrDrop(last)
            e.guiInstance[Slots.RowOneSlotFive] =
                if (player.shopHistory.lastSold.isEmpty()) ItemValue.placeholderDescripted(
                    Material.HOPPER,
                    "<green>Sell Item",
                    "Click items in your inventory to",
                    "sell them to this Shop!"
                ) else itemAsPurchaseHistory(player.shopHistory.lastSold.last().clone())

            sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                pitch = 2f
                volume = 2f
                playFor(e.player)
            }
        }

        val cmp = createCompound<Purchasable>({ deal ->
            val mc = Registry.ITEM.find(deal.item)
            val built = mc.build(player)!!
            built.amount = deal.amount
            built.meta {
                val lore = lore()!!
                val interm = mutableListOf(
                    "",
                    "<gray>Cost",
                    "<gold>${Formatting.withCommas(deal.price.toFloat().toBigDecimal())} Coins",
                    *deal.additionalItems.map { (extra, amount) ->
                        val baseName = Registry.ITEM.find(extra).name.str()
                        if (amount != 1)
                            "$baseName <dark_gray>x$amount"
                        else baseName
                    }.toTypedArray(),
                    "",
                    "<yellow>Click to trade!",
                )
                if (deal.amount < 32 && !deal.onlyOne && deal.additionalItems.isEmpty()) {
                    interm.add("<yellow>Right-Click for more trading")
                    interm.add("<yellow>options!")
                }
                lore.addAll(interm.map { text(it).noitalic() })
                lore(lore)
            }
            built
        }) { e, deal ->
            e.bukkitEvent.isCancelled = true
            if (e.bukkitEvent.isRightClick && deal.amount < 32 && !deal.onlyOne && deal.additionalItems.isEmpty()) {
                e.player.openGUI(chooseSpecificAmount(player, deal, model))
                return@createCompound
            }
            val price = deal.price.toFloat().toBigDecimal()
            if (player.purse < price) {
                sound(Sound.ENTITY_VILLAGER_NO) {
                    volume = 2f
                    playFor(e.player)
                }
                player.sendMessage("<red>You don't have enough coins!")
                return@createCompound
            }
            if (deal.additionalItems.isNotEmpty()) {
                // Checking that we have enough extra items
                val toRemove = mutableListOf<Pair<Identifier, Int>>()
                for ((additional, amount) in deal.additionalItems) {
                    if (!e.player.inventory.containsLeast(additional, amount)) {
                        sound(Sound.ENTITY_VILLAGER_NO) {
                            volume = 2f
                            playFor(e.player)
                        }
                        player.sendMessage("<red>You don't have enough items!")
                        return@createCompound
                    }
                    toRemove.add(Pair(additional, amount))
                }
                for ((built, amount) in toRemove) {
                    e.player.inventory.removeAnySlot(built, amount)
                }
            }

            player.purse -= transact(price, player.ref, Transaction.Kind.OUTGOING)
            sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                pitch = 2f
                volume = 2f
                playFor(e.player)
            }
            val dealItem = Registry.ITEM.find(deal.item)
            dealItem.amount = deal.amount
            val built = dealItem.build(player)!!
            modifyItemMeta(built, dealItem)

            e.player.giveOrDrop(built)
        }
        compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFiveSlotEight, cmp)
        cmp.addContent(model.items)
    }
}

private fun chooseSpecificAmount(
    player: MacrocosmPlayer,
    deal: Purchasable,
    model: ShopModel
): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Shop Trading Options")

    setupListeners(player)

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) {
            it.bukkitEvent.isCancelled = true
            it.player.closeInventory()
        }
        button(
            Slots.RowOneSlotFour,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To ${model.name}")
        ) {
            it.bukkitEvent.isCancelled = true
            it.player.openGUI(shopUi(player, model))
        }

        val pricePerOne = deal.price.toFloat() / deal.amount
        val mc = Registry.ITEM.find(deal.item)
        val cmp = createCompound<Int>({ count ->
            val price = ceil(pricePerOne * count).roundToInt()
            val built = mc.build(player)!!
            built.amount = count
            built.meta {
                val lore = lore()!!
                val interm = mutableListOf(
                    "",
                    "<gray>Cost",
                    "<gold>${Formatting.withCommas(price.toFloat().toBigDecimal())} Coins",
                    "",
                    "<yellow>Click to trade!",
                )
                lore.addAll(interm.map { text(it).noitalic() })
                lore(lore)

                displayName(displayName()!!.append(Component.text(" x$count").color(NamedTextColor.DARK_GRAY)))
            }
            built
        }) { e, count ->
            e.bukkitEvent.isCancelled = true
            val price = (pricePerOne * count).toBigDecimal()
            if (player.purse < price) {
                sound(Sound.ENTITY_VILLAGER_NO) {
                    volume = 2f
                    playFor(e.player)
                }
                player.sendMessage("<red>You don't have enough coins!")
                return@createCompound
            }

            player.purse -= transact(price, player.ref, Transaction.Kind.OUTGOING)
            sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                pitch = 2f
                volume = 2f
                playFor(e.player)
            }
            val dealItem = Registry.ITEM.find(deal.item)
            dealItem.amount = count
            val built = dealItem.build(player)!!
            modifyItemMeta(built, dealItem)

            e.player.giveOrDrop(built)
        }

        compoundSpace(Slots.RowFourSlotThree linTo Slots.RowFourSlotSeven, cmp)

        val d = if (deal.amount == 1) 5 else deal.amount
        cmp.addContent(listOf(1, d, d * 2, 32, 64).sorted())
    }
}
