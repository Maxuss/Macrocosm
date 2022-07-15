package space.maxus.macrocosm.players

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.server
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

class PlayerEquipment {
    var necklace: MacrocosmItem? = null
    var cloak: MacrocosmItem? = null
    var belt: MacrocosmItem? = null
    var gloves: MacrocosmItem? = null

    fun enumerate(): List<MacrocosmItem?> = listOf(necklace, cloak, belt, gloves)

    operator fun set(ty: ItemType, item: MacrocosmItem?) {
        when(ty) {
            ItemType.NECKLACE -> necklace = item
            ItemType.CLOAK -> cloak = item
            ItemType.BELT -> belt = item
            ItemType.GLOVES -> belt = item
            else -> return
        }
    }
}

object EquipmentHandler: Listener {
    val emptySlot = ItemValue.placeholderDescripted(Material.RED_STAINED_GLASS_PANE, "<red>None", "Put item here to equip it")

    fun menu(player: MacrocosmPlayer) {
        val eq = player.equipment

        val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
        val back = ItemValue.placeholderDescripted(Material.ARROW, "<yellow><!italic>Back", "Return to Macrocosm menu")

        val inv = server.createInventory(player.paper, 54, text("Equipment"))

        val items = mutableListOf(
            glass, glass, glass, glass, glass, glass, glass, glass, glass,
            glass, glass, glass, glass, emptySlot, glass, glass, glass, glass,
            glass, glass, glass, glass, emptySlot, glass, glass, glass, glass,
            glass, glass, glass, glass, emptySlot, glass, glass, glass, glass,
            glass, glass, glass, glass, emptySlot, glass, glass, glass, glass,
            back, glass, glass, glass, glass, glass, glass, glass, glass,
            )

        for(item in eq.enumerate()) {
            if(item != null) {
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
        if(inv != e.view.topInventory)
            return
        e.isCancelled = true

        val clickedSlot = e.slot

        if(clickedSlot == 45) {
            e.whoClicked.closeInventory()
            return
        }

        if(!slots.keys.contains(clickedSlot)) {
            return
        }

        val player = (e.whoClicked as? Player)?.macrocosm ?: return

        if(e.action.name.contains("PLACE") || e.action == InventoryAction.SWAP_WITH_CURSOR && e.cursor != null) {
            // placing item
            val stored = inv.getItem(clickedSlot)
            val mc = e.cursor?.macrocosm ?: return

            val requiredTy = slots[clickedSlot]!!
            if(mc.type != requiredTy)
                return

            player.equipment[mc.type] = mc
            inv.setItem(clickedSlot, e.cursor)
            if(stored != emptySlot) {
                e.whoClicked.setItemOnCursor(stored)
            } else {
                e.whoClicked.setItemOnCursor(null)
            }
        } else if(e.action.name.contains("PICKUP") && e.cursor.isAirOrNull()) {
            val stored = inv.getItem(clickedSlot)
            if(stored == emptySlot) {
                return
            }
            val ty = slots[clickedSlot]!!
            player.equipment[ty] = null
            inv.setItem(clickedSlot, emptySlot)
            e.whoClicked.setItemOnCursor(stored)
        }
    }

    val slots = sortedMapOf(
        13 to ItemType.NECKLACE,
        22 to ItemType.CLOAK,
        31 to ItemType.BELT,
        40 to ItemType.GLOVES
    )
}

fun ItemStack?.isAirOrNull() = this == null || this.type.isAir
