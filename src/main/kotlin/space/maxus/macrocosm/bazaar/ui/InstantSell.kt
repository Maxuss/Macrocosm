package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.items.meta
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
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.stripTags

fun sellInstantlyScreen(player: MacrocosmPlayer, item: Identifier): MacrocosmUI =
    macrocosmUi("bazaar_instant_sell", UIDimensions.FOUR_X_NINE) {
        val element = BazaarElement.idToElement(item)!!
        val elementName = element.name.color(null).str()
        val builtItem = element.build(player)!!
        val p = player.paper!!

        title = "${elementName.stripTags()} ▶ Instant Sell"

        page(0) {
            background()

            button(
                Slot.RowTwoSlotThree,
                modifyStackGenerateAmountButtonSell(
                    player,
                    elementName,
                    "<gold>Sell a stack!",
                    64,
                    item,
                    builtItem.clone()
                ).apply { amount = 64 }) { e ->
                Bazaar.instantSell(player, e.paper, item, 64)
                e.instance.reload()
            }

            val amountInInventory =
                p.inventory.filter { stack -> stack?.isSimilar(builtItem) == true }.sumOf { stack -> stack.amount }

            button(
                Slot.RowTwoSlotFive,
                modifyStackGenerateAmountButtonSell(
                    player,
                    elementName,
                    "<gold>Sell half your inventory!",
                    amountInInventory / 2,
                    item,
                    ItemStack(Material.CHEST)
                )
            ) { e ->
                if (amountInInventory / 2 > 0)
                    Bazaar.instantSell(player, e.paper, item, amountInInventory / 2)
                e.instance.reload()
            }

            button(
                Slot.RowTwoSlotSeven,
                modifyStackGenerateAmountButtonSell(
                    player,
                    elementName,
                    "<gold>Sell whole inventory!",
                    amountInInventory,
                    item,
                    ItemStack(Material.CHEST)
                )
            ) { e ->
                if (amountInInventory > 0)
                    Bazaar.instantSell(player, e.paper, item, amountInInventory)
                e.instance.reload()
            }

            button(
                Slot.RowThreeSlotEight,
                ItemValue.placeholderDescripted(
                    Material.OAK_SIGN,
                    "<green>Custom Amount",
                    "<dark_gray>$elementName",
                    "",
                    "Sell up to <green>${
                        Formatting.withCommas(
                            (Bazaar.table.itemData[item]!!.buy
                                .sumOf { it.qty }).toBigDecimal(), true
                        )
                    }x"
                )
            ) { e ->
                val inputAmountToSell = object : ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return "§eInput amount to sell:"
                    }

                    override fun isInputValid(context: ConversationContext, input: String): Boolean {
                        return try {
                            Integer.parseInt(input) > 0
                        } catch (e: NumberFormatException) {
                            false
                        }
                    }

                    override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                        val amount = Integer.parseInt(input)
                        confirmInstantSell(player, item, elementName, builtItem, amount).open(e.paper)
                        return Prompt.END_OF_CONVERSATION
                    }
                }
                e.paper.closeInventory()
                val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputAmountToSell)
                    .buildConversation(e.paper)
                conv.begin()
            }

            goBack(
                Slot.RowFourSlotFive,
                { openSpecificItemManagementMenu(player, item) }
            )
        }
    }

private fun confirmInstantSell(
    player: MacrocosmPlayer,
    item: Identifier,
    elementName: String,
    builtItem: ItemStack,
    amount: Int
): MacrocosmUI = macrocosmUi("bazaar_instant_sell_cofnirm", UIDimensions.FOUR_X_NINE) {
    title = "Confirm Instant Sell"

    page(0) {
        background()

        button(
            Slot.RowTwoSlotFive,
            modifyStackGenerateAmountButtonSell(
                player,
                elementName,
                "<green>Sell Custom Amount",
                amount,
                item,
                builtItem.clone()
            )
        ) { e ->
            Bazaar.instantSell(player, player.paper!!, item, amount)
            e.instance.reload()
        }

        goBack(
            Slot.RowTwoSlotFive,
            { sellInstantlyScreen(player, item) }
        )
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
    if (amount in 1..64)
        stack.amount = amount

    stack.meta {
        displayName(text(name).noitalic())

        val res = Bazaar.tryDoInstantSell(player, item, amount, false).get()
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
