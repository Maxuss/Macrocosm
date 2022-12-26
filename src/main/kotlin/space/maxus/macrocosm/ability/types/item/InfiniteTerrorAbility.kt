package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Bat
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic

object InfiniteTerrorAbility :
    AbilityBase(
        AbilityType.RIGHT_CLICK,
        "Infinite Terror",
        "Cast a <gold>Bat Swarm<gray> in front of yourself, dealing <red>30,000 ${Statistic.DAMAGE.display}<gray> to enemies, and healing you for <green>0.2%<gray> of damage dealt."
    ) {
    override val cost: AbilityCost = AbilityCost(600, 800, 5)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen
            val p = e.player.paper!!
            val damage = DamageCalculator.calculateMagicDamage(30000, .13f, e.player.stats()!!)

            val nearest = (p.getTargetBlockExact(6)?.location ?: p.eyeLocation.direction.multiply(2f).normalize()
                .relativeLocation(p.location)).clone()

            spawnTargetingBats(nearest)

            for (entity in nearest.getNearbyLivingEntities(5.0)) {
                if (entity is Player || entity is ArmorStand || entity is Bat)
                    continue

                entity.macrocosm!!.damage(damage, p)
                DamageHandlers.summonDamageIndicator(entity.location, damage)
                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.BLACK, 2f)
                    amount = 4
                    offset = Vector.getRandom()
                    spawnAt(entity.location.clone().add(nearest))
                }

            }

            e.player.heal(damage * .003f, e.player.stats()!!)
        }
    }

    private fun spawnTargetingBats(target: Location) {
        // amount of bats
        for (i in 0 until 15) {
            task(delay = 5L + i * 2) {
                val vector = Vector.getRandom()
                val relative = vector.relativeLocation(target)
                val entity = target.world.spawnEntity(relative, EntityType.BAT, CreatureSpawnEvent.SpawnReason.CUSTOM) {
                    it.persistentDataContainer.set(pluginKey("__IGNORE"), PersistentDataType.BYTE, 1)
                    (it as Bat).isAwake = true
                } as Bat

                sound(Sound.ENTITY_BLAZE_SHOOT) {
                    playAt(target)
                }
                var ticks = 0

                task(delay = 0L, period = 5L) { s ->
                    ticks++
                    if (ticks > 4) {
                        s.cancel()
                    } else
                        entity.velocity =
                            target.toVector().subtract(entity.location.toVector()).normalize().multiply(1.5f)
                }

                task(delay = 20L) {
                    entity.remove()
                }
            }
        }
    }
}
