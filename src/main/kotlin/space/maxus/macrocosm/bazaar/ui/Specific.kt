package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.Material
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.stripTags

fun openSpecificItemManagementMenu(player: MacrocosmPlayer, item: Identifier) = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    val coll = BazaarElement.idToCollection(item) ?: return@kSpigotGUI
    val element = BazaarElement.idToElement(item)!!
    val elementName = element.name.color(null).str()
    val builtItem = element.build(player)!!
    val p = player.paper!!

    defaultPage = 0
    title = text("${coll.displayName.stripTags()} ▶ $elementName")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(
            Slots.RowThreeSlotTwo,
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
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(buyInstantlyScreen(e.player.macrocosm!!, item))
        }

        val invAmount =
            p.inventory.filter { stack -> stack?.isSimilar(builtItem) == true }.sumOf { stack -> stack.amount }
                .toBigDecimal()
        button(
            Slots.RowThreeSlotThree,
            ItemValue.placeholderDescripted(
                Material.HOPPER,
                "<gold>Sell Instantly",
                "<dark_gray>$elementName",
                "",
                "Inventory: <green>${Formatting.withCommas(invAmount)} items",
                "",
                "<yellow>Click to pick amount!"
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(sellInstantlyScreen(e.player.macrocosm!!, item))
        }

        placeholder(Slots.RowThreeSlotFive, builtItem)

        button(
            Slots.RowThreeSlotSeven,
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
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(createBuyOrder(e.player.macrocosm!!, item))
        }

        button(
            Slots.RowThreeSlotEight,
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
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(createSellOrder(e.player.macrocosm!!, item))
        }

        button(
            Slots.RowOneSlotFive,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "<dark_gray>To Bazaar")
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(globalBazaarMenu(player))
        }
    }
}
