package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.emptySlots
import space.maxus.macrocosm.util.stripTags

internal fun buyInstantlyScreen(player: MacrocosmPlayer, item: Identifier): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    val element = BazaarElement.idToElement(item)!!
    val elementName = element.name.color(null).str()
    val builtItem = element.build(player)!!
    val p = player.paper!!

    title = text("${elementName.stripTags()} ▶ Instant Buy")
    defaultPage = 0

    page(0) {
            placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

            button(Slots.RowThreeSlotTwo, modifyStackGenerateAmountButtonBuy(player, elementName, "<green>Buy only <yellow>one<green>!", 1, item, builtItem.clone())) { e ->
                e.bukkitEvent.isCancelled = true
                Bazaar.instantBuy(player, e.player, item, 1)
            }

            button(Slots.RowThreeSlotFour, modifyStackGenerateAmountButtonBuy(player, elementName, "<green>Buy a stack!", 64, item, builtItem.clone()).apply { amount = 64 }) { e ->
                e.bukkitEvent.isCancelled = true
                Bazaar.instantBuy(player, e.player, item, 64)
            }

            val emptySlots = p.inventory.emptySlots * 64

            button(Slots.RowThreeSlotSix, modifyStackGenerateAmountButtonBuy(player, elementName, "<green>Fill my inventory!", emptySlots, item, ItemStack(Material.CHEST))) { e ->
                e.bukkitEvent.isCancelled = true
                Bazaar.instantBuy(player, e.player, item, emptySlots)
            }

            button(Slots.RowThreeSlotEight, ItemValue.placeholderDescripted(Material.OAK_SIGN, "<green>Custom Amount", "<dark_gray>$elementName", "", "Order up to <green>${Formatting.withCommas((Bazaar.table.itemData[item]!!.sell
                .sumOf { it.qty }).toBigDecimal(), true)}x")) { e ->
                e.bukkitEvent.isCancelled = true
                val inputCoinsPrompt = object: ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return ChatColor.YELLOW.toString() + "Input amount to order:"
                    }

                    override fun isInputValid(context: ConversationContext, input: String): Boolean {
                        return try {
                            Integer.parseInt(input)
                            true
                        } catch(e: NumberFormatException) {
                            false
                        }
                    }

                    override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                        val amount = Integer.parseInt(input)
                        e.player.openGUI(confirmInstantBuy(player, item, elementName, builtItem, amount))
                        return Prompt.END_OF_CONVERSATION
                    }
                }
                e.player.closeInventory()
                val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputCoinsPrompt).buildConversation(e.player)
                conv.begin()
            }

        button(Slots.RowOneSlotFive, ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "<dark_gray>To $elementName")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(openSpecificItemManagementMenu(player, item))
        }
    }
}

private fun confirmInstantBuy(player: MacrocosmPlayer, item: Identifier, elementName: String, builtItem: ItemStack, amount: Int) = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    title = text("Confirm Instant Buy")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(Slots.RowThreeSlotFive, modifyStackGenerateAmountButtonBuy(player, elementName, "<green>Buy Custom Amount", amount, item, builtItem.clone())) { e ->
            e.bukkitEvent.isCancelled = true
            Bazaar.instantBuy(player, player.paper!!, item, amount)
        }

        button(Slots.RowOneSlotFive, ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To $elementName > Instant Buy")) { e ->
            e.player.openGUI(buyInstantlyScreen(player, item))
        }
    }
}

internal fun modifyStackGenerateAmountButtonBuy(player: MacrocosmPlayer, elementName: String, name: String, amount: Int, item: Identifier, stack: ItemStack): ItemStack {
    stack.meta {
        displayName(text(name).noitalic())

        val res = Bazaar.tryDoInstantBuy(player, item, amount, false).get()
        val perUnit = res.coinsSpent / (res.amountBought.let { if(it <= 0) 1 else it }).toBigDecimal()

        val loreCompound = mutableListOf("<dark_gray>$elementName", "",  "<gray>Amount: <green>${Formatting.withCommas(amount.toBigDecimal(), true)}<gray>x", "")

        if(res.amountBought == 0) {
            loreCompound.add("<red>Could not find any orders!")
        } else {
            if (amount > 1) {
                loreCompound.add("<gray>Per unit: <gold>${Formatting.withCommas(perUnit)} coins")
            }
            loreCompound.add("<gray>Price: <gold>${Formatting.withCommas(res.coinsSpent)} coins")
            if (res.amountBought < amount) {
                loreCompound.add("<red>Only found <white>${res.amountBought}<red>x for sale!")
            }

            loreCompound.add("")
            if (res.coinsSpent > player.purse) {
                loreCompound.add("<red>You don't have enough Coins!")
            } else {
                loreCompound.add("<yellow>Click to order!")
            }
        }

        lore(loreCompound.map { text(it).noitalic() })
    }
    return stack
}
