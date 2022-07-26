package space.maxus.macrocosm.players

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.server
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.items.meta
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.metrics.report

class PlayerEquipment {
    var necklace: MacrocosmItem? = null
    var cloak: MacrocosmItem? = null
    var belt: MacrocosmItem? = null
    var gloves: MacrocosmItem? = null

    fun enumerate(): List<MacrocosmItem?> = listOf(necklace, cloak, belt, gloves)

    operator fun get(ty: ItemType): MacrocosmItem? {
        return when(ty) {
            ItemType.NECKLACE -> necklace
            ItemType.CLOAK -> cloak
            ItemType.BELT -> belt
            ItemType.GLOVES -> gloves
            else -> report("Unexpected item type: $ty") { null }
        }
    }

    operator fun set(ty: ItemType, item: MacrocosmItem?) {
        when (ty) {
            ItemType.NECKLACE -> necklace = item
            ItemType.CLOAK -> cloak = item
            ItemType.BELT -> belt = item
            ItemType.GLOVES -> belt = item
            else -> return
        }
    }
}

object EquipmentHandler : Listener {
    val emptyHelmet = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Helmet Slot")
    val emptyChest = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Chestplate Slot")
    val emptyLegs = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Leggings Slot")
    val emptyBoots = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Boots Slot")
    val emptyMainHand = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Hand Slot")
    val emptyOffHand = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Off Hand Slot")
    val emptyPet = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Empty Pet Slot")

    fun menu(player: MacrocosmPlayer) {
        val eq = player.equipment
        val p = player.paper ?: return

        val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
        val statBreakdown = ItemValue.placeholderDescripted(
            Material.ANVIL,
            "<light_purple>View Stats Breakdown",
            "View all of your stats in-depth!",
            "",
            "<yellow>Click to view stats breakdown!"
        )
        val close = ItemValue.placeholder(Material.BARRIER, "<red>Close")
        val back = ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To Macrocosm Menu")

        val helmet = p.inventory.helmet ?: emptyHelmet
        val chest = p.inventory.chestplate ?: emptyChest
        val legs = p.inventory.leggings ?: emptyLegs
        val boots = p.inventory.boots ?: emptyBoots
        val mainHand = if (p.inventory.itemInMainHand.isAirOrNull()) emptyMainHand else p.inventory.itemInMainHand
        val offHand = if (p.inventory.itemInOffHand.isAirOrNull()) emptyOffHand else p.inventory.itemInOffHand

        val activePet = player.activePet
        val pet = activePet?.prototype?.buildItem(player, player.ownedPets[activePet.hashKey]!!) ?: emptyPet

        val stats = player.stats()!!

        val generalStats = ItemValue.placeholder(Material.COMPASS, "<red>General Stats").apply {
            meta {
                lore(stats.iter().filter { (k, v) -> !k.secret && !k.specialized && v != 0f }
                    .map { (k, v) -> k.formatFancy(v) })
            }
        }
        val specializedStats = ItemValue.placeholder(Material.CLOCK, "<yellow>Specialized Stats").apply {
            meta {
                lore(stats.iter().filter { (k, v) -> k.specialized && v != 0f }.map { (k, v) -> k.formatFancy(v) })
            }
        }
        val secretStats = ItemValue.placeholder(Material.RECOVERY_COMPASS, "<dark_aqua>Secret Stats").apply {
            meta {
                lore(stats.iter().filter { (k, v) -> k.secret && v != 0f }.map { (k, v) -> k.formatFancy(v) })
            }
        }

        val inv = server.createInventory(player.paper, 54, text("Equipment"))

        val items = mutableListOf(
            glass, offHand, mainHand, glass, glass, glass, glass, glass, glass,
            glass, emptySlots[10], helmet, glass, glass, glass, glass, glass, glass,
            glass, emptySlots[19], chest, glass, glass, glass, generalStats, specializedStats, glass,
            glass, emptySlots[28], legs, glass, glass, glass, glass, secretStats, glass,
            glass, emptySlots[37], boots, glass, glass, glass, glass, glass, glass,
            glass, glass, pet, back, close, statBreakdown, glass, glass, glass,
        )

        for (item in eq.enumerate()) {
            if (item != null) {
                val (slot) = slots.entries.firstOrNull { it.value == item.type } ?: continue
                items[slot] = item.build(player) ?: continue
            }
        }

        inv.contents = items.toTypedArray()
        player.paper?.openInventory(inv)
    }

    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        if (!e.view.title().toLegacyString().contains("Equipment") || e.clickedInventory == null)
            return

        val inv = e.clickedInventory!!
        if (inv != e.view.topInventory)
            return
        e.isCancelled = true

        val clickedSlot = e.slot
        val player = (e.whoClicked as? Player)?.macrocosm ?: return

        when (clickedSlot) {
            48, 49 -> {
                e.whoClicked.closeInventory()
                return
            }
            50 -> {
                player.paper?.openGUI(statBreakdown(player))
            }
        }

        if (!slots.keys.contains(clickedSlot)) {
            return
        }

        val empty = emptySlots[clickedSlot]!!

        if (e.action.name.contains("PLACE") || e.action == InventoryAction.SWAP_WITH_CURSOR && e.cursor != null) {
            // placing item
            val stored = inv.getItem(clickedSlot)
            val mc = e.cursor?.macrocosm ?: return

            val requiredTy = slots[clickedSlot]!!
            if (mc.type != requiredTy)
                return

            player.equipment[mc.type] = mc
            inv.setItem(clickedSlot, e.cursor)
            if (stored != empty) {
                e.whoClicked.setItemOnCursor(stored)
            } else {
                e.whoClicked.setItemOnCursor(null)
            }
        } else if (e.action.name.contains("PICKUP") && e.cursor.isAirOrNull()) {
            val stored = inv.getItem(clickedSlot)
            if (stored == empty) {
                return
            }
            val ty = slots[clickedSlot]!!
            player.equipment[ty] = null
            inv.setItem(clickedSlot, empty)
            e.whoClicked.setItemOnCursor(stored)
        }
    }

    val slots = sortedMapOf(
        10 to ItemType.NECKLACE,
        19 to ItemType.CLOAK,
        28 to ItemType.BELT,
        37 to ItemType.GLOVES
    )

    val emptySlots = hashMapOf(
        10 to ItemValue.placeholderDescripted(
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            "<gray>Empty Equipment Slot",
            "<dark_gray> > Necklace"
        ),
        19 to ItemValue.placeholderDescripted(
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            "<gray>Empty Equipment Slot",
            "<dark_gray> > Cloak"
        ),
        28 to ItemValue.placeholderDescripted(
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            "<gray>Empty Equipment Slot",
            "<dark_gray> > Belt"
        ),
        37 to ItemValue.placeholderDescripted(
            Material.LIGHT_GRAY_STAINED_GLASS_PANE,
            "<gray>Empty Equipment Slot",
            "<dark_gray> > Gloves ",
            "<dark_gray> > Bracelet"
        ),
    )
}

fun ItemStack?.isAirOrNull() = this == null || this.type.isAir
