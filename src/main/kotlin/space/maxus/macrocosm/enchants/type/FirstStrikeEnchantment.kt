package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.pluginKey
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic

object FirstStrikeEnchantment :
    EnchantmentBase(
        "First Strike",
        "Increases ${Statistic.DAMAGE.display}<gray> you deal by <blue>[25]%<gray> for the first hit on a mob.",
        1..5,
        ItemType.melee(),
        conflicts = listOf("TRIPLE_STRIKE")
    ) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        if (pdc.has(pluginKey("strikes")))
            return
        pdc[pluginKey("strikes"), PersistentDataType.BYTE] = 1
        val multiplier = lvl * .25f
        e.damage *= (1 + multiplier)
    }
}

object TripleStrikeEnchantment :
    EnchantmentBase(
        "Triple-Strike",
        "Increases ${Statistic.DAMAGE.display}<gray> you deal by <blue>[10]%<gray> for the first three hits on a mob.",
        1..5,
        ItemType.melee(),
        conflicts = listOf("FIRST_STRIKE")
    ) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        if (!pdc.has(pluginKey("strikes"))) {
            pdc[pluginKey("strikes"), PersistentDataType.BYTE] = 1
        }
        val strikes = pdc[pluginKey("strikes"), PersistentDataType.BYTE]!!
        if (strikes > 3)
            return
        pdc[pluginKey("strikes"), PersistentDataType.BYTE] = (strikes + 1).toByte()

        val multiplier = lvl * .1f
        e.damage *= (1 + multiplier)
    }

}
