package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

object CleaveEnchantment: EnchantmentBase("Cleave", "", 1..6, ItemType.melee()) {
    override fun description(level: Int): List<Component> {
        val str = "Deals <green>${Formatting.stats((level * 4f).toBigDecimal(), true)}%<gray> of your ${Statistic.DAMAGE.display}<gray> to all mobs within <green>${Formatting.stats((level * 0.9f).toBigDecimal())}<gray> blocks of target."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if(e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if(!ok)
            return

        val multiplier = lvl * .03f
        val range = lvl * .9

        val damage = multiplier * e.damage
        for(entity in e.damaged.location.getNearbyLivingEntities(range) {
            it !is ArmorStand && it != e.damaged && it !is Player
        }) {
            entity.macrocosm!!.damage(damage, e.player.paper)
            DamageHandlers.summonDamageIndicator(entity.location, damage)
        }
    }
}
