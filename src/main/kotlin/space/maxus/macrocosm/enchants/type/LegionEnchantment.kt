package space.maxus.macrocosm.enchants.type

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType

object LegionEnchantment : UltimateEnchantment("Legion", "Increases <blue>ALL<gray> your stats by <yellow>[1]%<gray> for every player within <blue>10 blocks<gray> of you.", 1..5, ItemType.armor()) {
    @EventHandler
    fun onStatCalculation(e: PlayerCalculateStatsEvent) {
        val (ok, lvl) = ensureRequirementsStacking(
            e.player,
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
        )
        if (!ok)
            return
        val nearby = e.player.paper!!.location.getNearbyLivingEntities(10.0).filterIsInstance<Player>()
        if (nearby.isEmpty())
            return
        val clone = e.stats.clone()
        clone.multiply((.01f * lvl) * nearby.size)
    }
}
