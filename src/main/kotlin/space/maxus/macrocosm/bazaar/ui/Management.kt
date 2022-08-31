package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.bazaar.*
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.exceptions.MacrocosmThrowable
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.banking.Transaction
import space.maxus.macrocosm.players.banking.transact
import space.maxus.macrocosm.players.chat.ChatChannel
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.runCatchingReporting
import java.math.BigDecimal
import java.math.MathContext
import java.util.*

private object NullBazaarOrder : BazaarOrder(Identifier.NULL, UUID.randomUUID(), -1, -1L) {
    override val totalPrice: BigDecimal = BigDecimal.valueOf(0L)
}

internal fun manageOrders(player: MacrocosmPlayer): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    runCatchingReporting {
        defaultPage = 0
        title = text("Your Orders")
        val orders = Bazaar.getOrdersForPlayer(player.ref)
        if (orders.size > 28) {
            throw MacrocosmThrowable(
                "TOO_MANY_ORDERS",
                "You have too many orders active! This should not normally happen!"
            )
        }
        orders.padForward(28, NullBazaarOrder)
        val nullOrderGlass = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "")
        page(0) {
            placeholder(Slots.Border, ItemValue.placeholder(Material.LIME_STAINED_GLASS_PANE))

            val cmp = createRectCompound<BazaarOrder>(Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
                iconGenerator = { order ->
                    if (order is NullBazaarOrder)
                        return@createRectCompound nullOrderGlass
                    val resultItem =
                        BazaarElement.idToElement(order.item)?.build() ?: return@createRectCompound nullOrderGlass
                    resultItem.meta {
                        if (order is BazaarBuyOrder) {
                            displayName(
                                text(
                                    "<green><bold>BUY</bold> ${
                                        Formatting.withCommas(
                                            order.originalAmount.toBigDecimal(),
                                            true
                                        )
                                    }x </green>${displayName()!!.str()}"
                                ).noitalic()
                            )

                            val intermediaryLore = mutableListOf(
                                "<dark_gray>Worth ${truncateCoins(order.totalPrice)} coins",
                                "",
                                "<gray>Amount left: <green>${if (order.qty > 0) order.qty else "<bold>ORDER FILLED!"}",
                                "<gray>Sellers: ",
                            )
                            intermediaryLore.addAll(if (order.sellers.size > 0) {
                                order.sellers.map { seller ->
                                    Bukkit.getOfflinePlayer(seller).let { player ->
                                        "<dark_gray> - ${
                                            MacrocosmPlayer.loadPlayer(seller)?.rank?.playerName(player.name ?: "NULL")?.str() ?: "Unknown Seller!"
                                        }}"
                                    }
                                }
                            } else {
                                listOf("<dark_gray> - None")
                            })
                            intermediaryLore.add("")
                            intermediaryLore.addAll(
                                if (order.bought > 0) listOf(
                                    "<gray>Available to claim: <green>${order.bought}",
                                    "<yellow>Click to claim!"
                                ) else if (order.qty == 0 && order.bought == 0) listOf("<yellow>Click to clear!") else listOf(
                                    "<yellow>Click for options"
                                )
                            )
                            lore(intermediaryLore.map { text(it).noitalic() })
                        } else if (order is BazaarSellOrder) {
                            displayName(
                                text(
                                    "<green><bold>SELL</bold> ${
                                        Formatting.withCommas(
                                            order.originalAmount.toBigDecimal(),
                                            true
                                        )
                                    }x </green> ${displayName()!!.str()}"
                                ).noitalic()
                            )

                            val intermediaryLore = mutableListOf(
                                "<dark_gray>Worth ${truncateCoins(order.totalPrice)}",
                                "",
                                "<gray>Amount left: <green>${if (order.qty > 0) order.qty else "<bold>ORDER SOLD!"}",
                                "<gray>Buyers: ",
                            )
                            intermediaryLore.addAll(if (order.buyers.size > 0) {
                                order.buyers.map { seller ->
                                    Bukkit.getOfflinePlayer(seller).let { player ->
                                        "<dark_gray> - ${
                                            MacrocosmPlayer.loadPlayer(seller)?.rank?.playerName(player.name ?: "NULL")?.str() ?: "Unknown Buyer!"
                                        }}"
                                    }
                                }
                            } else {
                                listOf("<dark_gray> - None")
                            })
                            intermediaryLore.add("")
                            intermediaryLore.addAll(
                                if (order.sold > 0) listOf(
                                    "<gray>Available coins to claim: <green>${
                                        Formatting.withCommas(
                                            (order.pricePer.toBigDecimal() * order.sold.toBigDecimal() * BazaarIntrinsics.INCOMING_TAX_MODIFIER).round(
                                                MathContext(1)
                                            )
                                        )
                                    }", "<yellow>Click to claim!"
                                ) else if (order.qty == 0 && order.sold == 0) listOf("<yellow>Click to clear!") else listOf(
                                    "<yellow>Click for options"
                                )
                            )
                            lore(intermediaryLore.map { text(it).noitalic() })
                        }
                    }
                    resultItem
                },
                onClick = { e, order ->
                    e.bukkitEvent.isCancelled = true
                    if (order is BazaarBuyOrder) {
                        if (order.bought > 0) {
                            // claiming items
                            val mc = BazaarElement.idToElement(order.item) ?: return@createRectCompound
                            val item = mc.build(player) ?: return@createRectCompound
                            var amount = order.bought
                            while (amount > 64) {
                                amount -= 64
                                val clone = item.clone()
                                clone.amount = 64
                                e.player.giveOrDrop(clone)
                            }
                            val clone = item.clone()
                            clone.amount = amount
                            e.player.giveOrDrop(clone)
                            sound(Sound.ENTITY_PLAYER_LEVELUP) {
                                pitch = 2f
                                volume = 2f
                                playFor(e.player)
                            }
                            player.sendMessage(
                                ChatChannel.BAZAAR,
                                "<yellow>Claimed <green>${
                                    Formatting.withCommas(
                                        order.bought.toBigDecimal(),
                                        true
                                    )
                                }x<yellow> of ${mc.name.str()}<yellow>!"
                            )
                            order.bought = 0
                            if(order.qty == 0) {
                                Bazaar.table.popOrder(order)
                                e.guiInstance.reloadCurrentPage()
                            }
                        } else {
                            // managing order
                            sound(Sound.UI_BUTTON_CLICK) {
                                volume = 2f
                                playFor(e.player)
                            }
                            e.player.openGUI(manageSingleOrder(player, order))
                        }
                    } else if (order is BazaarSellOrder) {
                        if (order.sold > 0) {
                            // claiming coins
                            val mc = BazaarElement.idToElement(order.item) ?: return@createRectCompound
                            player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                            val coins = transact(
                                order.sold.toBigDecimal() * order.pricePer.toBigDecimal() * BazaarIntrinsics.INCOMING_TAX_MODIFIER,
                                player.ref,
                                Transaction.Kind.INCOMING
                            )
                            player.purse += coins
                            sound(Sound.ENTITY_PLAYER_LEVELUP) {
                                pitch = 2f
                                volume = 2f
                                playFor(e.player)
                            }
                            order.sold = 0
                            player.sendMessage(
                                ChatChannel.BAZAAR,
                                "<yellow>Claimed <gold><hover:show_text:'<gray>Includes incoming <green>Bazaar Tax<gray>:<br><gold>${BazaarIntrinsics.INCOMING_TAX_MODIFIER}'>${
                                    Formatting.withCommas(coins)
                                } coins</hover><yellow> from selling <green>${
                                    Formatting.withCommas(
                                        order.sold.toBigDecimal(),
                                        true
                                    )
                                }x<yellow> of ${mc.name.str()}<yellow> to ${order.buyers.size} buyers!"
                            )
                            if(order.qty == 0) {
                                Bazaar.table.popOrder(order)
                                e.guiInstance.reloadCurrentPage()
                            }
                        } else {
                            // clearing order
                            sound(Sound.UI_BUTTON_CLICK) {
                                volume = 2f
                                playFor(e.player)
                            }
                            e.player.openGUI(manageSingleOrder(player, order))
                        }
                    }
                }
            )

            val allOrders = Bazaar.getOrdersForPlayer(player.ref)
            cmp.addContent(allOrders.padForward(28, NullBazaarOrder))

            button(
                Slots.RowOneSlotFive,
                ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "<dark_gray>To Bazaar")
            ) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.openGUI(globalBazaarMenu(player))
            }
        }
    }
}

internal fun manageSingleOrder(player: MacrocosmPlayer, order: BazaarOrder) = kSpigotGUI(GUIType.THREE_BY_NINE) {
    defaultPage = 0
    title = text("Manage Order")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
        button(
            Slots.RowOneSlotFive,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "<dark_gray>To Your Orders")
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(manageOrders(player))
        }
        if (order is BazaarBuyOrder) {
            // delete order
            button(
                Slots.RowTwoSlotFive,
                ItemValue.placeholderDescripted(
                    Material.BARRIER,
                    "<red>Delete Order",
                    "Completely deletes this order",
                    "and refunds <gold>80%<gray> of coins spent",
                    "",
                    "<red>Be careful!",
                    "",
                    "<yellow>I'm sure, delete this order"
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                sound(Sound.ENTITY_BLAZE_DEATH) {
                    pitch = 2f
                    volume = 2f
                    playFor(e.player)
                }
                Bazaar.table.popOrder(order)
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                val amount =
                    transact(order.totalPrice * .8.toBigDecimal(), e.player.uniqueId, Transaction.Kind.INCOMING)
                player.purse += amount
                player.sendMessage(
                    ChatChannel.BAZAAR,
                    "<yellow>Refunded <gold>${Formatting.withCommas(amount)} coins<yellow> from deleting a bazaar order."
                )
                e.player.openGUI(manageOrders(player))
            }
        } else if(order is BazaarSellOrder) {
            button(
                Slots.RowTwoSlotFive,
                ItemValue.placeholderDescripted(
                    Material.BARRIER,
                    "<red>Delete Order",
                    "Completely deletes this order",
                    "and returns items contained in it.",
                    "",
                    "<yellow>I'm sure, delete this order"
                )
            ) {  e ->
                e.bukkitEvent.isCancelled = true
                sound(Sound.ENTITY_BLAZE_DEATH) {
                    pitch = 2f
                    volume = 2f
                    playFor(e.player)
                }
                Bazaar.table.popOrder(order)
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                var itemsToRefund = order.qty
                val item = BazaarElement.idToElement(order.item)!!.build(player)!!
                while(itemsToRefund > 64) {
                    itemsToRefund -= 64
                    val c = item.clone()
                    c.amount = 64
                    e.player.giveOrDrop(c)
                }
                item.amount = itemsToRefund
                e.player.giveOrDrop(item)
                player.sendMessage(
                    ChatChannel.BAZAAR,
                    "<yellow>Refunded <green>${Formatting.withCommas(itemsToRefund.toBigDecimal(), true)}x<yellow> items from deleting a bazaar order."
                )
                e.player.openGUI(manageOrders(player))
            }
        }
    }
}

private fun truncateCoins(amount: BigDecimal): String {
    return if (amount > 1_000_000_000.toBigDecimal()) {
        // > 1B
        "${Formatting.stats((amount / 1_000_000_000.toBigDecimal()).round(MathContext(3)))}B"
    } else if (amount > 1_000_000.toBigDecimal()) {
        // > 1M
        "${Formatting.stats((amount / 1_000_000.toBigDecimal()).round(MathContext(3)))}M"
    } else if (amount > 1_000.toBigDecimal()) {
        // > 1k
        "${Formatting.stats((amount / 1_000.toBigDecimal()).round(MathContext(3)))}k"
    } else {
        // default
        Formatting.stats(amount.round(MathContext(3)))
    }
}
