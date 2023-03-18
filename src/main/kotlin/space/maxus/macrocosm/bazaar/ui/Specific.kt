package space.maxus.macrocosm.bazaar.ui

import org.bukkit.Material
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.stripTags

fun openSpecificItemManagementMenu(player: MacrocosmPlayer, item: Identifier): MacrocosmUI =
    macrocosmUi("bazaar_manage_specific", UIDimensions.FOUR_X_NINE) {
        val coll = BazaarElement.idToCollection(item) ?: return@macrocosmUi
        val element = BazaarElement.idToElement(item)!!
        val elementName = element.name.color(null).str()
        val builtItem = element.build(player)!!
        val p = player.paper!!

        title = "${coll.displayName.stripTags()} ▶ $elementName"

        pageLazy {
            background()

            switchUi(
                Slot.RowTwoSlotTwo,
                { buyInstantlyScreen(player, item) },
                ItemValue.placeholderDescripted(
                    Material.GOLDEN_HORSE_ARMOR,
                    "<green>Buy Instantly",
                    "<dark_gray>$elementName",
                    "",
                    "Price per unit: <gold>${
                        Formatting.withFullCommas((Bazaar.table.nextSellOrder(item)?.pricePer ?: .0).toBigDecimal())
                    } coins",
                    "Stack price: <gold>${
                        Formatting.withFullCommas(
                            Bazaar.tryDoInstantBuy(player, item, 64, false).get().coinsSpent
                        )
                    }",
                    "",
                    "<yellow>Click to pick amount!"
                )
            )

            val invAmount =
                p.inventory.filter { stack -> stack?.isSimilar(builtItem) == true }.sumOf { stack -> stack.amount }
                    .toBigDecimal()
            switchUi(
                Slot.RowTwoSlotThree,
                { sellInstantlyScreen(player, item) },
                ItemValue.placeholderDescripted(
                    Material.HOPPER,
                    "<gold>Sell Instantly",
                    "<dark_gray>$elementName",
                    "",
                    "Inventory: <green>${Formatting.withCommas(invAmount)} items",
                    "",
                    "<yellow>Click to pick amount!"
                )
            )

            placeholder(Slot.RowTwoSlotFive, builtItem)

            switchUi(
                Slot.RowTwoSlotSeven,
                { createBuyOrder(player, item) },
                ItemValue.placeholderDescripted(
                    Material.FILLED_MAP,
                    "<green>Create Buy Order",
                    "<dark_gray>$elementName",
                    "",
                    "<green>Top Orders:",
                    *Bazaar.table.itemData[item]!!.buy.take(5).map { order ->
                        "<dark_gray>- <gold>${Formatting.withFullCommas(order.pricePer.toBigDecimal())} coins<gray> each | <green>${
                            Formatting.withCommas(
                                order.qty.toBigDecimal()
                            )
                        }<gray>x"
                    }.toTypedArray(),
                    "",
                    "<yellow>Click to setup buy order!"
                )
            )

            switchUi(
                Slot.RowTwoSlotEight,
                { createSellOrder(player, item) },
                ItemValue.placeholderDescripted(
                    Material.MAP,
                    "<green>Create Sell Order",
                    "<dark_gray>$elementName",
                    "",
                    "<gold>Top Offers:",
                    *Bazaar.table.itemData[item]!!.sell.take(7).map { order ->
                        "<dark_gray>- <gold>${Formatting.withFullCommas(order.pricePer.toBigDecimal())} coins<gray> each | <green>${
                            Formatting.withCommas(
                                order.qty.toBigDecimal()
                            )
                        }<gray>x"
                    }.toTypedArray(),
                    "",
                    "Inventory: <green>${Formatting.withCommas(invAmount)} items",
                    "",
                    "<yellow>Click to setup sell order!"
                )
            )

            goBack(
                Slot.RowFourSlotFive,
                { globalBazaarMenu(player) },
                "Bazaar"
            )
        }
    }
