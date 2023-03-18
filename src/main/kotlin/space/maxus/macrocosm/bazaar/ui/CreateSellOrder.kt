package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.entity.Player
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

internal fun createSellOrder(player: MacrocosmPlayer, item: Identifier): MacrocosmUI =
    macrocosmUi("create_sell_order", UIDimensions.FOUR_X_NINE) {
        val element = BazaarElement.idToElement(item)!!
        val elementName = element.name.color(null).str()
        val builtItem = element.build(player)!!
        val p = player.paper!!

        title = "${elementName.stripTags()} ▶ Create Sell Order"

        page(0) {
            background()

            val amountInInventory =
                p.inventory.filter { stack -> stack?.isSimilar(builtItem) == true }.sumOf { stack -> stack.amount }

            button(
                Slot.RowTwoSlotTwo,
                modifyStackGenerateAmountButtonSellOrder(
                    "<green>Sell inventory!",
                    amountInInventory,
                    ItemStack(Material.CHEST)
                )
            ) { e ->
                if (amountInInventory > 0)
                    e.instance.switch(createSellOrderManagePrice(player, item, amountInInventory))
            }

            button(
                Slot.RowThreeSlotEight, ItemValue.placeholderDescripted(
                    Material.OAK_SIGN,
                    "<green>Custom Amount",
                    "<dark_gray>$elementName",
                    "",
                    "Sell custom amount of",
                    "items."
                )
            ) { e ->
                val inputCoinsPrompt = object : ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return ChatColor.YELLOW.toString() + "Input amount to order:"
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
                        createSellOrderManagePrice(player, item, amount).open(e.paper)
                        return END_OF_CONVERSATION
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

private fun createSellOrderManagePrice(
    player: MacrocosmPlayer,
    item: Identifier,
    amount: Int
): MacrocosmUI = macrocosmUi(
    "create_sell_order_manage",
    UIDimensions.FOUR_X_NINE
) {
    val element = BazaarElement.idToElement(item)!!
    val elementName = element.name.color(null).str()
    val builtItem = element.build(player)!!

    title = "${elementName.stripTags()} ▶ Create Sell Order"

    page(0) {
        background()

        val topOrder = Bazaar.table.nextBuyOrder(item)?.pricePer
        val topSellOrder = Bazaar.table.nextSellOrder(item)?.pricePer
        button(
            Slot.RowTwoSlotTwo,
            constructPriceButton(
                builtItem,
                topSellOrder,
                arrayOf("<red>Could not find a sell order", "<red>of the same type to get the top", "<red>price!"),
                "<gold>Same as Top Order",
                "Create a Buy Order with the",
                "same price per unit as the",
                "current top order"
            )
        ) { e ->
            if (topSellOrder != null) {
                e.instance.switch(
                    confirmSellOrder(
                        player,
                        e.paper,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        topSellOrder.toBigDecimal()
                    )
                )
            }
        }

        button(
            Slot.RowTwoSlotFour,
            constructPriceButton(
                ItemStack(Material.GOLD_NUGGET),
                topSellOrder?.let { order -> order - 0.1 },
                arrayOf("<red>Could not find a sell order", "<red>of the same type to get the top", "<red>price!"),
                "<gold>Top Order -0.1",
                "Create a Buy Order with the",
                "price <gold>0.1<gray> lower than the",
                "current top order"
            )
        ) { e ->
            if (topSellOrder != null) {
                e.instance.switch(
                    confirmSellOrder(
                        player,
                        e.paper,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        (topSellOrder - 0.1).toBigDecimal()
                    )
                )
            }
        }

        val tenPercentSpread = if (topSellOrder != null && topOrder != null) (topSellOrder - topOrder) * .10 else null
        button(
            Slot.RowTwoSlotSix,
            constructPriceButton(
                ItemStack(Material.GOLDEN_HORSE_ARMOR),
                tenPercentSpread,
                arrayOf("<red>Could not find enough buy and", "<red>sell orders to calculate spread!"),
                "<gold>10% of Spread",
                "Create a Buy Order with the",
                "price equal to the difference",
                "between the highest buy order and",
                "the lowest sell order."
            )
        ) { e ->
            if (tenPercentSpread != null) {
                e.instance.switch(
                    confirmSellOrder(
                        player,
                        e.paper,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        tenPercentSpread.toBigDecimal()
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
                    return ChatColor.YELLOW.toString() + "Input price per:"
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

                    confirmSellOrder(
                        player,
                        e.paper,
                        item,
                        builtItem,
                        elementName,
                        amount,
                        pricePer.toBigDecimal()
                    ).open(e.paper)
                    return END_OF_CONVERSATION
                }
            }
            e.paper.closeInventory()
            val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputPricePer)
                .buildConversation(e.paper)
            conv.begin()
        }

        goBack(
            Slot.RowLastSlotFive,
            { openSpecificItemManagementMenu(player, item) },
            elementName
        )
    }
}

private fun confirmSellOrder(
    player: MacrocosmPlayer,
    p: Player,
    item: Identifier,
    baseItem: ItemStack,
    elementName: String,
    amount: Int,
    pricePer: BigDecimal
): MacrocosmUI = macrocosmUi(
    "sell_order_confirm",
    UIDimensions.FOUR_X_NINE
) {
    title = "Confirm Sell Order"

    pageLazy {
        background()
        val c = baseItem.clone()

        c.meta {
            displayName(text("<green>Confirm Sell Order").noitalic())

            val lore = mutableListOf(
                "<dark_gray>$elementName",
                "",
                "<gray>Price per unit: <gold>${Formatting.withCommas(pricePer)} coins",
                "<gray>Amount to sell: <green>${Formatting.withCommas(amount.toBigDecimal(), true)}",
                "",
                "<gray>Expected Profit: <gold>${Formatting.withCommas((amount.toBigDecimal() * pricePer) * BazaarIntrinsics.INCOMING_TAX_MODIFIER)} coins"
            )

            val contains = p.inventory.containsAtLeast(baseItem, amount)
            if (!contains)
                lore.addAll(arrayOf("", "<red>Not enough items to sell!"))
            else
                lore.addAll(arrayOf("", "<yellow>Click to setup order!"))
            lore(lore.map { text(it).noitalic() })
        }

        button(Slot.RowTwoSlotFive, c) { e ->
            sound(Sound.UI_BUTTON_CLICK) {
                volume = 2f
                playFor(e.paper)
            }

            val contains = e.paper.inventory.containsAtLeast(baseItem, amount)

            if (contains) {
                Bazaar.createSellOrder(player, e.paper, item, amount, pricePer.toDouble())
            }

            e.paper.closeInventory()
        }

        goBack(
            Slot.RowFourSlotFive,
            { openSpecificItemManagementMenu(player, item) },
            elementName
        )
    }
}

internal fun modifyStackGenerateAmountButtonSellOrder(
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
            "<dark_gray>Sell Order Setup",
            "",
            "<gray>Amount: <green>${Formatting.withCommas(amount.toBigDecimal(), true)}<gray>x",
            "",
            "<yellow>Click to proceed!"
        )

        lore(loreCompound.map { text(it).noitalic() })
    }
    return stack
}
