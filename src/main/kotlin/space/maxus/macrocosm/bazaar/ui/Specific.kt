package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.Material
import space.maxus.macrocosm.bazaar.BazaarElement
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

    defaultPage = 0
    title = text("${coll.displayName.stripTags()} ▶ $elementName")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(
            Slots.RowThreeSlotTwo,
            ItemValue.placeholderDescripted(
                Material.CHEST,
                "<green>Instant Buy",
                "Instantly buy items at possibly",
                "higher cost than <green>regular<gray> buy orders."
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(buyInstantlyScreen(e.player.macrocosm!!, item))
        }

        button(
            Slots.RowThreeSlotThree,
            ItemValue.placeholderDescripted(
                Material.CHEST,
                "<green>Instant Sell",
                "Instantly sell items at possibly",
                "lower profit than <green>regular<gray> sell orders."
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(sellInstantlyScreen(e.player.macrocosm!!, item))
        }

        placeholder(Slots.RowThreeSlotFive, builtItem)

        button(
            Slots.RowThreeSlotSeven,
            ItemValue.placeholderDescripted(
                Material.HOPPER,
                "<green>Create Buy Order",
                "Create a buy order with own price",
                "per item and amount."
            )
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(createBuyOrder(e.player.macrocosm!!, item))
        }

        button(
            Slots.RowThreeSlotEight,
            ItemValue.placeholderDescripted(
                Material.HOPPER,
                "<green>Create Sell Order",
                "Create a sell order with own price",
                "per item and amount."
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
