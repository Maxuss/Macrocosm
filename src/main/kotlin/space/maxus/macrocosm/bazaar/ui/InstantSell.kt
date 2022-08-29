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
import space.maxus.macrocosm.util.stripTags

fun sellInstantlyScreen(player: MacrocosmPlayer, item: Identifier): GUI<ForInventoryFourByNine> =
    kSpigotGUI(GUIType.FOUR_BY_NINE) {
        val element = BazaarElement.idToElement(item)!!
        val elementName = element.name.color(null).str()
        val builtItem = element.build(player)!!
        val p = player.paper!!

        title = text("${elementName.stripTags()} ▶ Instant Sell")
        defaultPage = 0

        page(0) {
            placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

            button(
                Slots.RowThreeSlotTwo,
                modifyStackGenerateAmountButtonSell(
                    player,
                    elementName,
                    "<green>Sell only <yellow>one<green>!",
                    1,
                    item,
                    builtItem.clone()
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                Bazaar.instantSell(player, e.player, item, 1)
                e.guiInstance.reloadCurrentPage()
            }

            button(
                Slots.RowThreeSlotFour,
                modifyStackGenerateAmountButtonSell(
                    player,
                    elementName,
                    "<green>Sell a stack!",
                    64,
                    item,
                    builtItem.clone()
                ).apply { amount = 64 }) { e ->
                e.bukkitEvent.isCancelled = true
                Bazaar.instantSell(player, e.player, item, 64)
                e.guiInstance.reloadCurrentPage()
            }

            val amountInInventory =
                p.inventory.filter { stack -> stack?.isSimilar(builtItem) == true }.sumOf { stack -> stack.amount }

            button(
                Slots.RowThreeSlotSix,
                modifyStackGenerateAmountButtonSell(
                    player,
                    elementName,
                    "<green>Sell all in inventory!",
                    amountInInventory,
                    item,
                    ItemStack(Material.CHEST)
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                Bazaar.instantSell(player, e.player, item, amountInInventory)
                e.guiInstance.reloadCurrentPage()
            }

            button(Slots.RowThreeSlotEight,
                ItemValue.placeholderDescripted(Material.OAK_SIGN,
                    "<green>Custom Amount",
                    "<dark_gray>$elementName",
                    "",
                    "Sell up to <green>${
                        Formatting.withCommas((Bazaar.table.itemData[item]!!.buy
                            .sumOf { it.qty }).toBigDecimal(), true
                        )
                    }x"
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                val inputAmountToSell = object : ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return ChatColor.YELLOW.toString() + "Input amount to sell:"
                    }

                    override fun isInputValid(context: ConversationContext, input: String): Boolean {
                        return try {
                            Integer.parseInt(input)
                            true
                        } catch (e: NumberFormatException) {
                            false
                        }
                    }

                    override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                        val amount = Integer.parseInt(input)
                        e.player.openGUI(confirmInstantSell(player, item, elementName, builtItem, amount))
                        return Prompt.END_OF_CONVERSATION
                    }
                }
                e.player.closeInventory()
                val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputAmountToSell)
                    .buildConversation(e.player)
                conv.begin()
            }

            button(
                Slots.RowOneSlotFive,
                ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "<dark_gray>To $elementName")
            ) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.openGUI(openSpecificItemManagementMenu(player, item))
            }
        }
    }

private fun confirmInstantSell(
    player: MacrocosmPlayer,
    item: Identifier,
    elementName: String,
    builtItem: ItemStack,
    amount: Int
): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    title = text("Confirm Instant Sell")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(
            Slots.RowThreeSlotFive,
            modifyStackGenerateAmountButtonSell(
                player,
                elementName,
                "<green>Sell Custom Amount",
                amount,
                item,
                builtItem.clone()
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            Bazaar.instantSell(player, player.paper!!, item, amount)
            e.guiInstance.reloadCurrentPage()
        }

        button(
            Slots.RowOneSlotFive,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To $elementName > Instant Buy")
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(sellInstantlyScreen(player, item))
        }
    }
}


internal fun modifyStackGenerateAmountButtonSell(
    player: MacrocosmPlayer,
    elementName: String,
    name: String,
    amount: Int,
    item: Identifier,
    stack: ItemStack,
    checkZero: Boolean = true
): ItemStack {
    stack.meta {
        displayName(text(name).noitalic())

        val res = Bazaar.tryDoInstantSell(item, amount, false).get()
        val perUnit = res.totalProfit / res.amountSold.let { if (it <= 0) 1 else it }.toBigDecimal()

        val loreCompound = mutableListOf(
            "<dark_gray>$elementName",
            "",
            "<gray>Amount: <green>${Formatting.withCommas(amount.toBigDecimal(), true)}<gray>x",
            ""
        )

        if (checkZero && res.amountSold == 0) {
            loreCompound.add("<red>Could not find any orders!")
        } else {
            if (amount > 1) {
                loreCompound.add("<gray>Per unit: <gold>${Formatting.withCommas(perUnit)} coins")
            }
            loreCompound.add("<gray>Profit: <gold>${Formatting.withCommas(res.totalProfit)} coins")
            if (res.amountSold < amount) {
                loreCompound.add("<red>Only found <white>${res.amountSold}<red>x items in orders!")
            }

            loreCompound.add("")
            if (player.paper!!.inventory.containsAtLeast(stack, res.amountSold)) {
                loreCompound.add("<red>You don't have enough items to sell!")
            } else {
                loreCompound.add("<yellow>Click to order!")
            }
        }

        lore(loreCompound.map { text(it).noitalic() })
    }
    return stack
}
