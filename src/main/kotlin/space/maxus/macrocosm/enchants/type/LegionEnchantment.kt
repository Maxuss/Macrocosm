package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.text.comp

object LegionEnchantment: UltimateEnchantment("Legion", "", 1..5, ItemType.armor()) {
    override fun description(level: Int): List<Component> {
        val str = "Increases <blue>ALL<gray> your stats by <yellow>$level%<gray> for every player within <blue>10 blocks<gray> of you."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onStatCalculation(e: PlayerCalculateStatsEvent) {
        val (ok, lvl) = ensureRequirementsStacking(e.player, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)
        if(!ok)
            return
        val nearby = e.player.paper!!.location.getNearbyLivingEntities(10.0).filterIsInstance<Player>()
        if(nearby.isEmpty())
            return
        val clone = e.stats.clone()
        clone.multiply((.01f * lvl) * nearby.size)
    }
}
