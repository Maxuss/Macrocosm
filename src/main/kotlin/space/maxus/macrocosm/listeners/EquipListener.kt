package space.maxus.macrocosm.listeners

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm

@Deprecated("Apparently, PlayerArmorChangeEvent is called even if you set player's armor yourself, providing a huge CPU hot spot.")
object EquipListener : Listener {
    @Deprecated("View deprecated message for the listener object")
    @EventHandler
    fun onEquip(e: PlayerArmorChangeEvent) {
        if (e.player.macrocosm == null)
            return

        val new = e.newItem
        if (new == null || new.type.isAir)
            return
        val slot = when (e.slotType) {
            PlayerArmorChangeEvent.SlotType.HEAD -> EquipmentSlot.HEAD
            PlayerArmorChangeEvent.SlotType.CHEST -> EquipmentSlot.CHEST
            PlayerArmorChangeEvent.SlotType.LEGS -> EquipmentSlot.LEGS
            PlayerArmorChangeEvent.SlotType.FEET -> EquipmentSlot.FEET
        }
        e.player.inventory.setItem(slot, new.macrocosm!!.build(e.player.macrocosm!!)!!)
    }
}
