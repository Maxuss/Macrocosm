package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import org.bukkit.ChatColor
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
import space.maxus.macrocosm.util.stripTags
import java.math.BigDecimal

internal fun createBuyOrder(player: MacrocosmPlayer, item: Identifier): GUI<ForInventoryFourByNine> =
    kSpigotGUI(GUIType.FOUR_BY_NINE) {
        val element = BazaarElement.idToElement(item)!!
        val elementName = element.name.color(null).str()
        val builtItem = element.build(player)!!

        defaultPage = 0
        title = text("${elementName.stripTags()} ▶ Create Buy Order")

        page(0) {
            placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

            button(
                Slots.RowThreeSlotTwo,
                modifyStackGenerateAmountButtonBuy(
                    player,
                    elementName,
                    "<green>Order a <yellow>stack<green>!",
                    64,
                    item,
                    builtItem.clone()
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.openGUI(createBuyOrderManagePrice(player, item, 64))
            }

            button(
                Slots.RowThreeSlotFour,
                modifyStackGenerateAmountButtonBuy(
                    player,
                    elementName,
                    "<green>Order a Big Stack!",
                    160,
                    item,
                    builtItem.clone()
                ).apply { amount = 64 }) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.openGUI(createBuyOrderManagePrice(player, item, 160))
            }

            button(
                Slots.RowThreeSlotSix,
                modifyStackGenerateAmountButtonBuy(
                    player,
                    elementName,
                    "<green>A thousand!",
                    1024,
                    item,
                    ItemStack(Material.CHEST)
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.openGUI(createBuyOrderManagePrice(player, item, 1024))
            }

            button(
                Slots.RowThreeSlotEight,
                ItemValue.placeholderDescripted(
                    Material.OAK_SIGN,
                    "<green>Custom Amount",
                    "<dark_gray>$elementName",
                    "",
                    "Buy custom amount of items."
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                val inputCoinsPrompt = object : ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return ChatColor.YELLOW.toString() + "Input amount to order:"
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
                        e.player.openGUI(createBuyOrderManagePrice(player, item, amount))
                        return Prompt.END_OF_CONVERSATION
                    }
                }
                e.player.closeInventory()
                val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputCoinsPrompt)
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

private fun createBuyOrderManagePrice(
    player: MacrocosmPlayer,
    item: Identifier,
    amount: Int
): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    val element = BazaarElement.idToElement(item)!!
    val elementName = element.name.color(null).str()
    val builtItem = element.build(player)!!

    defaultPage = 0
    title = text("${elementName.stripTags()} ▶ Create Buy Order")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val topOrder = Bazaar.table.nextBuyOrder(item)?.pricePer
        val topSellOrder = Bazaar.table.nextSellOrder(item)?.pricePer
        button(
            Slots.RowThreeSlotTwo,
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
            e.bukkitEvent.isCancelled = true
            if (topOrder != null) {
                e.player.openGUI(confirmBuyOrder(player, item, builtItem, elementName, amount, topOrder.toBigDecimal()))
            }
        }

        button(
            Slots.RowThreeSlotFour,
            constructPriceButton(
                ItemStack(Material.NAME_TAG),
                topOrder?.let { order -> order + 0.1 },
                arrayOf("<red>Could not find a buy order", "<red>of the same type to get the top", "<red>price!"),
                "<gold>Top Order plus 0.1",
                "Create a Buy Order with the",
                "price <gold>0.1<gray> higher than the",
                "current top order"
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            if (topOrder != null) {
                e.player.openGUI(
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
            Slots.RowThreeSlotSix,
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
            e.bukkitEvent.isCancelled = true
            if (fivePercentSpread != null) {
                e.player.openGUI(
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
            Slots.RowThreeSlotEight,
            ItemValue.placeholderDescripted(Material.OAK_SIGN, "<gold>Custom Price", "Name your own price.")
        ) { e ->
            e.bukkitEvent.isCancelled = true

            val inputPricePer = object : ValidatingPrompt() {
                override fun getPromptText(context: ConversationContext): String {
                    return ChatColor.YELLOW.toString() + "Input price per:"
                }

                override fun isInputValid(context: ConversationContext, input: String): Boolean {
                    return try {
                        java.lang.Double.parseDouble(input)
                        true
                    } catch (e: NumberFormatException) {
                        false
                    }
                }

                override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                    val pricePer = java.lang.Double.parseDouble(input)
                    e.player.openGUI(
                        confirmBuyOrder(
                            player,
                            item,
                            builtItem,
                            elementName,
                            amount,
                            pricePer.toBigDecimal()
                        )
                    )
                    return Prompt.END_OF_CONVERSATION
                }
            }
            e.player.closeInventory()
            val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputPricePer)
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

private fun confirmBuyOrder(
    player: MacrocosmPlayer,
    item: Identifier,
    baseItem: ItemStack,
    elementName: String,
    amount: Int,
    pricePer: BigDecimal
): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    defaultPage = 0
    title = text("Confirm Buy Order")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

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

        button(Slots.RowThreeSlotFive, baseItem) { e ->
            e.bukkitEvent.isCancelled = true
            sound(Sound.UI_BUTTON_CLICK) {
                volume = 2f
                playFor(e.player)
            }

            Bazaar.createBuyOrder(player, e.player, item, amount, pricePer.toDouble())

            e.player.closeInventory()
        }

        button(
            Slots.RowOneSlotFive,
            ItemValue.placeholderDescripted(
                Material.ARROW,
                "<green>Go Back",
                "<dark_gray>To $elementName ▶ Create Buy Order"
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(openSpecificItemManagementMenu(player, item))
        }
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
