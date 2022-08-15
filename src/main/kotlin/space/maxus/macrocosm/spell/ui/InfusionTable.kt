package space.maxus.macrocosm.spell.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.SpellScroll
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.slayer.ui.LinearInventorySlots
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.generic.itemSlot
import space.maxus.macrocosm.util.generic.mutableButton

fun displaySelectEssence(p: MacrocosmPlayer, scroll: Boolean, selectedEssence: HashMap<InventorySlot, EssenceType>, currentlySelectedSlot: SingleInventorySlot<out ForInventory>): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    // essence selection
    title = text("Select Essence")
    defaultPage = 0

    page(0) {
        val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
        placeholder(Slots.All, glass)

        val cmp = createCompound<String>(iconGenerator = {
            if (it == "NULL")
                glass
            else {
                val ty = EssenceType.valueOf(it)
                val amount = p.availableEssence[ty]!!
                if (amount > 0) {
                    val item = ItemValue.placeholder(ty.displayItem, "${ty.display}<white>: $amount")
                    item.meta {
                        val newLore = mutableListOf<Component>()
                        ty.descript(newLore)
                        newLore.addAll(
                            listOf(
                                "",
                                "<gray>You have: <green>$amount<gray> Essence"
                            ).map { ele -> text(ele).noitalic() }
                        )
                        lore(newLore)
                    }
                    item
                } else
                    ItemValue.placeholderDescripted(
                        Material.GRAY_TERRACOTTA,
                        "<gray>Unknown Essence<white>: 0",
                        "This is an unknown essence type!",
                        "Find it somewhere in the world",
                        "first to reveal it."
                    )
            }
        }, onClick = { e, it ->
            e.bukkitEvent.isCancelled = true
            if (it == "NULL")
                return@createCompound
            val ty = EssenceType.valueOf(it)
            val availableAmount = p.availableEssence[ty]!!
            if (availableAmount <= 0)
                return@createCompound
            if (availableAmount < 10) {
                p.sendMessage("<red>Not enough essence! Requires at least 10 essence.")
                return@createCompound
            }
            selectedEssence[currentlySelectedSlot.inventorySlot] = ty
            e.player.openGUI(displayInfusionTable(p, scroll, selectedEssence))
        })
        compoundSpace(
            LinearInventorySlots(
                listOf(
                    InventorySlot(5, 3),
                    InventorySlot(5, 5),
                    InventorySlot(5, 7),
                    InventorySlot(3, 3),
                    InventorySlot(3, 5),
                    InventorySlot(3, 7),
                    InventorySlot(4, 5)
                )
            ),
            cmp
        )

        cmp.addContent(
            listOf(
                EssenceType.FIRE.name, EssenceType.WATER.name, EssenceType.FROST.name,
                EssenceType.LIFE.name, EssenceType.SHADE.name, EssenceType.DEATH.name,
                EssenceType.CONNECTION.name
            )
        )
    }
}

fun displayInfusionTable(
    p: MacrocosmPlayer,
    scroll: Boolean = false,
    selectedEssence: HashMap<InventorySlot, EssenceType> = hashMapOf()
): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Infusion Table")

    val essenceEmpty = ItemValue.placeholderDescripted(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Essence Slot", "<dark_gray> > <gray>No Essence", " ", "<yellow>Click to add essence!")
    var scrollPersistent = scroll
    val cantInfuse = ItemValue.placeholderDescripted(Material.RED_TERRACOTTA, "<red>Can not infuse!", "Place essence in the essence", "slots and an empty spell", "scroll in the middle to", "infuse!")
    val canInfuse = ItemValue.placeholderDescripted(Material.GREEN_TERRACOTTA, "<gree>Infuse!", "<yellow>Click to infuse scroll", "<yellow>with new powers!")
    page(0) {
        title = text("Infusion Table")
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
        slots.forEach { slot ->
            val ty = selectedEssence[slot.inventorySlot]
            val item = if(ty == null) essenceEmpty else ItemValue.placeholder(ty.displayItem, "${ty.display} Essence")
            button(slot, item) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.openGUI(displaySelectEssence(p, scrollPersistent, selectedEssence, slot))
            }
        }
        val emptyScroll = ItemValue.placeholderDescripted(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Scroll Slot", "<dark_gray> > <gray>No Scroll", " ", "<yellow>Click with a scroll to add", "<yellow>it!")
        itemSlot(Slots.RowThreeSlotFive, emptyScroll) { self, e ->
            scrollPersistent = false
            val item = e.player.itemOnCursor.macrocosm ?: return@itemSlot self.currentDisplay
            if(item !is SpellScroll || item.spell != null)
                return@itemSlot self.currentDisplay
            val cursor = e.player.itemOnCursor
            if(cursor.amount > 1) {
                cursor.amount -= 1
                e.player.setItemOnCursor(cursor)
            } else {
                e.player.setItemOnCursor(null)
            }
            scrollPersistent = true
            e.guiInstance.reloadCurrentPage()
            item.build(p)!!.apply { amount = 1 }
        }
        mutableButton(Slots.RowTwoSlotFive, if(scrollPersistent) canInfuse else cantInfuse) { _, e ->
            e.bukkitEvent.isCancelled = true
            scrollPersistent = false
            e.bukkitEvent.inventory.setItem(Slots.RowThreeSlotFive.inventorySlot.realSlotIn(InventoryDimensions(9, 6))!!, emptyScroll)
            cantInfuse
        }
    }
}

private val slots = listOf(
    Slots.RowTwoSlotThree,
    Slots.RowTwoSlotSeven,
    Slots.RowFourSlotThree,
    Slots.RowFourSlotSeven,
)
