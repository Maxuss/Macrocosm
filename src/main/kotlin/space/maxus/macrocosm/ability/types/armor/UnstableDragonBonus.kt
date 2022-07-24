package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.roundToInt

object UnstableDragonBonus : FullSetBonus(
    "Unstable Blood",
    "Sometimes strikes nearby enemies with lightning every <green>10 seconds<gray>, dealing <red>1000%<gray> of your <blue>${Statistic.CRIT_DAMAGE.display}<gray>."
) {
    override fun registerListeners() {
        task(delay = 20L, period = 120L) {
            for (player in Bukkit.getOnlinePlayers().parallelStream()) {
                val mc = player.macrocosm ?: continue
                if (!ensureSetRequirement(mc))
                    continue
                val dmg = DamageCalculator.calculateMagicDamage(
                    (mc.stats()!!.critDamage * 10).roundToInt(),
                    .4f,
                    mc.stats()!!
                )
                for (entity in player.location.getNearbyLivingEntities(10.0, 4.0).parallelStream()) {
                    if (entity is Player || entity is ArmorStand)
                        continue
                    entity.world.strikeLightningEffect(entity.location)
                    entity.macrocosm?.damage(dmg, player)
                    DamageHandlers.summonDamageIndicator(entity.location, dmg, DamageType.ELECTRIC)
                }
            }
        }
    }
}
