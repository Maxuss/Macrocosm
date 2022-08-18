package space.maxus.macrocosm.listeners

import net.axay.kspigot.gui.InventoryDimensions
import net.axay.kspigot.gui.Slots
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.giveOrDrop

object InventoryListeners: Listener {
    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        if(e.view.title().str().contains("Infusion Table")) {
            (e.player as Player).giveOrDrop(e.inventory.getItem(
                Slots.RowThreeSlotFive.inventorySlot.realSlotIn(
                    InventoryDimensions(9, 6)
                )!!) ?: return)
        }
    }
}
