package space.maxus.macrocosm.bazaar.ui

import net.axay.kspigot.items.meta
import org.bukkit.Material
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarCategory
import space.maxus.macrocosm.bazaar.BazaarCollection
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.PageBuilder
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.pad
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.stripTags
import java.util.*

fun globalBazaarMenu(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("bazaar_global", UIDimensions.SIX_X_NINE) {
    title = "Bazaar"

    for (category in BazaarCategory.values()) {
        pageLazy(category.ordinal) {
            background()
            addNavButtons(player)

            val items =
                category.items.map { Optional.of(it) }.padForward(24, Optional.empty<BazaarCollection>()).toList()

            compound(Slot.RowTwoSlotThree rect Slot.RowFiveSlotEight, items.sortedBy { if (it.isEmpty) 1 else 0 },
                { coll ->
                    if (coll.isEmpty)
                        ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
                    else {
                        val collection = coll.get()
                        val stack = collection.displayItem.clone()
                        val averageBuy =
                            collection.items.map { Bazaar.table.nextBuyOrder(it)?.pricePer ?: .0 }.average()
                        val averageSell =
                            collection.items.map { Bazaar.table.nextSellOrder(it)?.pricePer ?: .0 }.average()
                        stack.meta {
                            displayName(text(collection.displayName).noitalic())

                            val lore = mutableListOf<String>()
                            lore.add("<dark_gray>${collection.items.size} products")
                            lore.add("")
                            lore.add(
                                "Instant Buy Price: <gold>${Formatting.withFullCommas(averageSell.toBigDecimal())} coins",
                            )
                            lore.add(
                                "Instant Sell Price: <gold>${Formatting.withFullCommas(averageBuy.toBigDecimal())} coins",
                            )
                            lore.add(
                                ""
                            )
                            lore.add("<yellow>Click to view products!")

                            lore(lore.map { text("<gray>$it").noitalic() })
                        }
                        stack
                    }
                },
                { e, coll ->
                    if (coll.isPresent) {
                        val collection = coll.get()
                        e.instance.switch(specificCollectionMenu(player, collection))
                    }
                }
            )
        }
    }
}

private fun specificCollectionMenu(player: MacrocosmPlayer, collection: BazaarCollection): MacrocosmUI = macrocosmUi("bazaar_specific_collection", UIDimensions.FOUR_X_NINE) {
        title = collection.displayName.stripTags()

        page(0) {
            background()

            transparentCompound(Slot.RowTwoSlotTwo rect Slot.RowTwoSlotEight,
                collection.items.pad(7, Identifier.NULL).padForward(7, Identifier.NULL),
                { id ->
                    if (id.isNull())
                        ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
                    else {
                        val product = BazaarElement.idToElement(id)!!
                        val stack = product.build(player)!!
                        val data = Bazaar.table.itemData[id]!!
                        stack.meta {
                            val lore = mutableListOf(
                                "<dark_gray>${product.rarity.name.capitalized()} commodity",
                                "",
                                "<gray>Instant Buy Price: ${
                                    data.nextSellOrder()?.pricePer?.let {
                                        "<gold>${
                                            Formatting.withFullCommas(
                                                it.toBigDecimal()
                                            )
                                        } coins"
                                    } ?: "<red>No Sell Orders!"
                                }",
                                "<gray>Instant Sell Price: ${
                                    data.nextBuyOrder()?.pricePer?.let {
                                        "<gold>${
                                            Formatting.withFullCommas(
                                                it.toBigDecimal()
                                            )
                                        } coins"
                                    } ?: "<red>No Buy Orders!"
                                }",
                                "",
                                "<yellow>Click to view details!"
                            )
                            lore(lore.map { text(it).noitalic() })
                        }
                        stack
                    }
                },
                { e, id ->
                    if (id.isNotNull()) {
                        e.instance.switch(openSpecificItemManagementMenu(player, id))
                    }
                }
            )

            goBack(
                Slot.RowSixSlotFive,
                { globalBazaarMenu(player) },
                "Bazaar"
            )
        }
    }

private fun PageBuilder.addNavButtons(player: MacrocosmPlayer) {
    // category navigation buttons
    for (category in BazaarCategory.values()) {
        val row = category.ordinal
        changePage(
            Slot(row, 0),
            category.ordinal,
            ItemValue.placeholderDescripted(
                category.displayItem,
                category.displayName,
                *category.description.reduceToList(21).filter { it.isNotBlank() }.toTypedArray()
            ),
        )
    }

    // manage orders button
    switchUi(
        Slot.RowSixSlotTwo,
        { manageOrders(player) },
        ItemValue.placeholderDescripted(Material.BOOK, "<green>Manage Orders", "Manage your current orders")
    )
}
