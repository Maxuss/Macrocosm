package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

object FireAspectEnchantment :
    EnchantmentBase("Fire Aspect", "", 1..5, ItemType.melee(), conflicts = listOf("FROST_ASPECT")) {
    override fun description(level: Int): List<Component> {
        val str =
            "<gray>Sets your enemies <gold>on fire<gray>, dealing <green>${10 + (level * 10)}%</green> of your ${Statistic.DAMAGE.display}<gray> per second for <green>${level + 3}s</green>"
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onHit(e: PlayerDealDamageEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        if (pdc.has(pluginKey("fa_ticks")))
            return
        if (pdc.has(pluginKey("fa_ticks_frost")))
            pdc[pluginKey("fa_ticks_frost"), PersistentDataType.LONG] = 0

        e.damaged.isVisualFire = true
        val stats = e.player.stats()!!
        val damagedStats = e.damaged.macrocosm!!.calculateStats()
        val damage = (.1f + (lvl * .1f)) * DamageCalculator.calculateStandardReceived(
            DamageCalculator.calculateStandardDealt(
                stats.damage,
                stats
            ).first, damagedStats
        )
        task(period = 20L) {
            if (e.damaged.isDead) {
                it.cancel()
                return@task
            }
            val ticksLeft =
                e.damaged.persistentDataContainer[pluginKey("fa_ticks"), PersistentDataType.LONG]
                    ?: (20L * (lvl + 3))

            if (ticksLeft <= 0) {
                e.damaged.isVisualFire = false
                e.damaged.persistentDataContainer.remove(pluginKey("fa_ticks"))
                it.cancel()
                return@task
            }

            e.damaged.persistentDataContainer[pluginKey("fa_ticks"), PersistentDataType.LONG] =
                ticksLeft - 20

            e.damaged.macrocosm!!.damage(damage, e.player.paper)
            DamageHandlers.summonDamageIndicator(e.damaged.location, damage, DamageType.FIRE)
        }
    }
}

object FrostAspectEnchantment :
    EnchantmentBase("Frost Aspect", "", 1..5, ItemType.melee(), conflicts = listOf("FIRE_ASPECT")) {
    override fun description(level: Int): List<Component> {
        val str =
            "<aqua>Freezes<gray> your enemies, dealing <green>${10 + (level * 5)}%</green> of your ${Statistic.DAMAGE.display}<gray> per second for <green>${level}s</green>"
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onHit(e: PlayerDealDamageEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        if (pdc.has(pluginKey("fa_ticks_frost")) || pdc.has(pluginKey("fa_ticks")))
            return

        val stats = e.player.stats()!!
        val damagedStats = e.damaged.macrocosm!!.calculateStats()
        val damage = (.1f + (lvl * .05f)) * DamageCalculator.calculateStandardReceived(
            DamageCalculator.calculateStandardDealt(
                stats.damage,
                stats
            ).first, damagedStats
        )
        task(period = 20L) {
            if (e.damaged.isDead) {
                it.cancel()
                return@task
            }
            val ticksLeft =
                e.damaged.persistentDataContainer[pluginKey("fa_ticks_frost"), PersistentDataType.LONG]
                    ?: (20L * (lvl + 3))

            if (ticksLeft <= 0) {
                e.damaged.persistentDataContainer.remove(pluginKey("fa_ticks_frost"))
                it.cancel()
                return@task
            }

            e.damaged.persistentDataContainer[pluginKey("fa_ticks_frost"), PersistentDataType.LONG] =
                ticksLeft - 20
            e.damaged.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 22, 2, true, false, false))

            e.damaged.macrocosm!!.damage(damage, e.player.paper)
            DamageHandlers.summonDamageIndicator(e.damaged.location, damage, DamageType.FROST)
        }
    }
}
