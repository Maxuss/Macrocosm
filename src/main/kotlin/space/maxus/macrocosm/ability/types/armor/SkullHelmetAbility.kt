package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import org.bukkit.Particle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerLeftClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import java.util.*

object SkullHelmetAbility : AbilityBase(
    AbilityType.PASSIVE,
    "Skull Mind",
    "All your melee attacks deal no damage, but instead emit a <aqua>Magic Ray<gray> that extends up to 6 blocks forward, deals <red>60%<gray> of your ${Statistic.DAMAGE.display}<gray>, scaling off your ${Statistic.INTELLIGENCE.display}<gray>."
) {
    override fun registerListeners() {
        listen<PlayerLeftClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HEAD))
                return@listen
            summonRay(e.player.paper ?: return@listen)
        }
        listen<EntityDamageByEntityEvent> { e ->
            if (e.damager !is Player || !ensureRequirements((e.damager as Player).macrocosm!!, EquipmentSlot.HEAD))
                return@listen
            e.isCancelled = true
            summonRay(e.damager as Player)
        }
    }

    private fun summonRay(by: Player) {
        val mc = by.macrocosm!!
        val stats = mc.stats()!!
        val dmg = DamageCalculator.calculateMagicDamage(
            stats.damage * (1 + (stats.strength / 100f)) * (1 + (stats.damageBoost / 100f)) * .6f,
            .1f,
            stats
        )

        val hit = mutableListOf<UUID>()
        TitansLightning.renderLine(
            by.eyeLocation.add(vec(y = -.5f)),
            by.eyeLocation.add(by.eyeLocation.direction.multiply(10f))
        ) {
            particle(Particle.END_ROD) {
                extra = 0f
                amount = 2

                spawnAt(it)
            }

            it.getNearbyLivingEntities(1.0) { entity ->
                entity !is Player && entity !is ArmorStand && !hit.contains(
                    entity.uniqueId
                )
            }.forEach { entity ->
                hit.add(entity.uniqueId)
                entity.macrocosm?.damage(dmg, by)
                DamageHandlers.summonDamageIndicator(entity.location, dmg)
            }
        }
    }
}
