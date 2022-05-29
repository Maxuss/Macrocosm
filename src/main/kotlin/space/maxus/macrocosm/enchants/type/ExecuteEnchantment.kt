package space.maxus.macrocosm.enchants.type

import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic

object ExecuteEnchantment : EnchantmentBase("Execute", "Increases ${Statistic.DAMAGE.display}<gray> you deal by <green>[0.2]%<gray> for every percent of ${Statistic.HEALTH.display}<blue> missing<gray> on your target.", 1..6, ItemType.melee(), conflicts = listOf("PROSECUTE")) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val modifier = lvl * .002f
        val mc = e.damaged.macrocosm!!
        val damagedStats = mc.calculateStats()
        val missing = damagedStats.health - mc.currentHealth
        val ratio = missing / damagedStats.health
        e.damage *= (1 + (ratio * modifier * 100))
    }
}

object ProsecuteEnchantment : EnchantmentBase("Prosecute", "Increases ${Statistic.DAMAGE.display}<gray> you deal by <green>[0.15]%<gray> for every percent of ${Statistic.HEALTH.display}<gray> your target has.", 1..6, ItemType.melee(), conflicts = listOf("EXECUTE")) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val modifier = lvl * .0015f
        val mc = e.damaged.macrocosm!!
        val damagedStats = mc.calculateStats()
        val ratio = mc.currentHealth / damagedStats.health
        e.damage *= (1 + (ratio * modifier * 100))
    }
}
