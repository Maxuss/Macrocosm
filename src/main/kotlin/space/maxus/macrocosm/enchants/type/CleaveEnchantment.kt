package space.maxus.macrocosm.enchants.type

import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic

object CleaveEnchantment : EnchantmentBase(
    "Cleave",
    "Deals <green>[4]%<gray> of your ${Statistic.DAMAGE.display}<gray> to all mobs within <green>[0.9]<gray> blocks of target.",
    1..6,
    ItemType.melee()
) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val multiplier = lvl * .03f
        val range = lvl * .9

        val damage = multiplier * e.damage
        for (entity in e.damaged.location.getNearbyLivingEntities(range) {
            it !is ArmorStand && it != e.damaged && it !is Player
        }) {
            entity.macrocosm!!.damage(damage, e.player.paper)
            DamageHandlers.summonDamageIndicator(entity.location, damage)
        }
    }
}
