package space.maxus.macrocosm.listeners

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm

object EquipListener: Listener {
    @EventHandler
    fun onEquip(e: PlayerArmorChangeEvent) {
        val new = e.newItem
        if(new == null || new.type.isAir)
            return
        val slot = when(e.slotType) {
            PlayerArmorChangeEvent.SlotType.HEAD -> EquipmentSlot.HEAD
            PlayerArmorChangeEvent.SlotType.CHEST -> EquipmentSlot.CHEST
            PlayerArmorChangeEvent.SlotType.LEGS -> EquipmentSlot.LEGS
            PlayerArmorChangeEvent.SlotType.FEET -> EquipmentSlot.FEET
        }
        e.player.inventory.setItem(slot, new.macrocosm!!.build(e.player.macrocosm!!)!!)
    }
}
