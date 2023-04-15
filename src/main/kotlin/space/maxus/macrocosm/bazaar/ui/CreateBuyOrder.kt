package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.bazaar.BazaarIntrinsics
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
import java.math.BigDecimal

internal fun createBuyOrder(player: MacrocosmPlayer, item: Identifier): MacrocosmUI =
    macrocosmUi("bazaar_buy_order", UIDimensions.FOUR_X_NINE) {
        val element = BazaarElement.idToElement(item)!!
        val elementName = element.name.color(null).str()
        val builtItem = element.build(player)!!

        title = "${elementName.stripTags()} ▶ Create Buy Order"

        pageLazy {
            background()

            switchUi(
                Slot.RowTwoSlotTwo,
                { createBuyOrderManagePrice(player, item, 64) },
                modifyStackGenerateAmountButtonBuyOrder(
                    "<green>Order a <yellow>stack<green>!",
                    64,
                    builtItem.clone()
                )
            )

            switchUi(
                Slot.RowTwoSlotFour,
                { createBuyOrderManagePrice(player, item, 160) },
                modifyStackGenerateAmountButtonBuyOrder(
                    "<green>Order a Big Stack!",
                    160,
                    builtItem.clone()
                ).apply { amount = 64 })

            switchUi(
                Slot.RowTwoSlotSix,
                { createBuyOrderManagePrice(player, item, 1024) },
                modifyStackGenerateAmountButtonBuyOrder(
                    "<green>A thousand!",
                    1024,
                    ItemStack(Material.CHEST)
                )
            )

            button(
                Slot.RowTwoSlotEight,
                ItemValue.placeholderDescripted(
                    Material.OAK_SIGN,
                    "<green>Custom Amount",
                    "<dark_gray>$elementName",
                    "",
                    "Buy custom amount of items."
                )
            ) { e ->
                val inputCoinsPrompt = object : ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return "§eInput amount to order:"
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
                        createBuyOrderManagePrice(player, item, amount).open(e.paper)
                        return Prompt.END_OF_CONVERSATION
                    }
                }
                e.paper.closeInventory()
                val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputCoinsPrompt)
                    .buildConversation(e.paper)
                conv.begin()
            }

            goBack(Slot.RowFourSlotFive, { openSpecificItemManagementMenu(player, item) }, elementName)
        }
    }

private fun createBuyOrderManagePrice(
    player: MacrocosmPlayer,
    item: Identifier,
    amount: Int
): MacrocosmUI = macrocosmUi("create_buy_order_manage", UIDimensions.FOUR_X_NINE) {
    val element = BazaarElement.idToElement(item)!!
    val elementName = element.name.color(null).str()
    val builtItem = element.build(player)!!

    title = "${elementName.stripTags()} ▶ Create Buy Order"

    pageLazy {
        background()

        val topOrder = Bazaar.table.nextBuyOrder(item)?.pricePer
        val topSellOrder = Bazaar.table.nextSellOrder(item)?.pricePer
        button(
            Slot.RowTwoSlotTwo,
            constructPriceButton(
                builtItem,
                topOrder,
                arrayOf("<red>Could not find a buy order", "<red>of the same type to get the top", "<red>price!"),
                "<gold>Same as Top Order",
                "Create a Buy Order with the",
                "same price per unit as the",
                "current top order"
            )
        ) { e ->
            if (topOrder != null) {
                e.instance.switch(
                    confirmBuyOrder(
                        player,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        topOrder.toBigDecimal()
                    )
                )
            }
        }

        button(
            Slot.RowTwoSlotFour,
            constructPriceButton(
                ItemStack(Material.GOLD_NUGGET),
                topOrder?.let { order -> order + 0.1 },
                arrayOf("<red>Could not find a buy order", "<red>of the same type to get the top", "<red>price!"),
                "<gold>Top Order +0.1",
                "Create a Buy Order with the",
                "price <gold>0.1<gray> higher than the",
                "current top order"
            )
        ) { e ->
            if (topOrder != null) {
                e.instance.switch(
                    confirmBuyOrder(
                        player,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        (topOrder + 0.1).toBigDecimal()
                    )
                )
            }
        }

        val fivePercentSpread = if (topSellOrder != null && topOrder != null) (topSellOrder - topOrder) * .05 else null
        button(
            Slot.RowTwoSlotSix,
            constructPriceButton(
                ItemStack(Material.GOLDEN_HORSE_ARMOR),
                fivePercentSpread,
                arrayOf("<red>Could not find enough buy and", "<red>sell orders to calculate spread!"),
                "<gold>5% of Spread",
                "Create a Buy Order with the",
                "price equal to the difference",
                "between the highest buy order and",
                "the lowest sell order."
            )
        ) { e ->
            if (fivePercentSpread != null) {
                e.instance.switch(
                    confirmBuyOrder(
                        player,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        fivePercentSpread.toBigDecimal()
                    )
                )
            }
        }

        button(
            Slot.RowTwoSlotEight,
            ItemValue.placeholderDescripted(Material.OAK_SIGN, "<gold>Custom Price", "Name your own price.")
        ) { e ->
            val inputPricePer = object : ValidatingPrompt() {
                override fun getPromptText(context: ConversationContext): String {
                    return "§eInput price per:"
                }

                override fun isInputValid(context: ConversationContext, input: String): Boolean {
                    return try {
                        java.lang.Double.parseDouble(input) > 0
                    } catch (e: NumberFormatException) {
                        false
                    }
                }

                override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                    val pricePer = java.lang.Double.parseDouble(input)
                    confirmBuyOrder(
                        player,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        pricePer.toBigDecimal()
                    ).open(e.paper)
                    return Prompt.END_OF_CONVERSATION
                }
            }
            e.paper.closeInventory()
            val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputPricePer)
                .buildConversation(e.paper)
            conv.begin()
        }

        goBack(Slot.RowFourSlotFive, { openSpecificItemManagementMenu(player, item) }, elementName)
    }
}

private fun confirmBuyOrder(
    player: MacrocosmPlayer,
    item: Identifier,
    baseItem: ItemStack,
    elementName: String,
    amount: Int,
    pricePer: BigDecimal
): MacrocosmUI = macrocosmUi("buy_confirm_order", UIDimensions.FOUR_X_NINE) {
    title = "Confirm Buy Order"

    pageLazy {
        background()

        baseItem.meta {
            displayName(text("<green>Confirm Buy Order").noitalic())

            val lore = mutableListOf(
                "<dark_gray>$elementName",
                "",
                "<gray>Price per unit: <gold>${Formatting.withCommas(pricePer)} coins",
                "<gray>Amount to order: <green>${Formatting.withCommas(amount.toBigDecimal(), true)}",
                "",
                "<gray>Total Price: <gold>${Formatting.withCommas(pricePer * amount.toBigDecimal())} coins"
            )
            if (player.purse < pricePer * BazaarIntrinsics.OUTGOING_TAX_MODIFIER)
                lore.addAll(arrayOf("", "<red>Not enough coins!"))
            else
                lore.addAll(arrayOf("", "<yellow>Click to setup order!"))
            lore(lore.map { text(it).noitalic() })
        }

        button(Slot.RowTwoSlotFive, baseItem) { e ->
            sound(Sound.UI_BUTTON_CLICK) {
                volume = 2f
                playFor(e.paper)
            }

            Bazaar.createBuyOrder(player, e.paper, item, amount, pricePer.toDouble())

            e.paper.closeInventory()
        }

        goBack(
            Slot.RowFourSlotFive,
            { openSpecificItemManagementMenu(player, item) },
            "$elementName ▶ Create Buy Order"
        )
    }
}

internal fun constructPriceButton(
    baseItem: ItemStack,
    pricePer: Double?,
    fallbackMessage: Array<out String>,
    name: String,
    vararg desc: String
): ItemStack {
    val c = baseItem.clone()
    c.meta {
        displayName(text(name).noitalic())
        val lore = mutableListOf(*desc, "")
        if (pricePer != null) {
            lore.addAll(
                arrayOf(
                    "Price per unit: <gold>${Formatting.withCommas(pricePer.toBigDecimal())} coins",
                    "",
                    "<yellow>Click to confirm order!"
                )
            )
        } else {
            lore.addAll(fallbackMessage)
        }
        lore(lore.map { text("<gray>$it").noitalic() })
    }
    return c
}

internal fun modifyStackGenerateAmountButtonBuyOrder(
    name: String,
    amount: Int,
    stack: ItemStack
): ItemStack {
    if (amount in 1..64) {
        stack.amount = amount
    }
    stack.meta {
        displayName(text(name).noitalic())

        val loreCompound = mutableListOf(
            "<dark_gray>Buy Order Setup",
            "",
            "<gray>Amount: <green>${Formatting.withCommas(amount.toBigDecimal(), true)}<gray>x",
            "",
            "<yellow>Click to proceed!"
        )

        lore(loreCompound.map { text(it).noitalic() })
    }
    return stack
}
