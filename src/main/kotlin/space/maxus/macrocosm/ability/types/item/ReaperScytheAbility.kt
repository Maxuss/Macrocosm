package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic

object ReaperScytheAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Soul Harvest", "Swipe your scythe, dealing <red>15,000 ${Statistic.DAMAGE.display}<gray> and lots of knockback to nearby entities.<br>Heal <red>50 ${Statistic.HEALTH.display}<gray> pet hit enemy.") {
    override val cost: AbilityCost = AbilityCost(300, cooldown = 4)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val loc = e.player.paper!!.eyeLocation
            sound(Sound.ENTITY_CREEPER_DEATH) {
                pitch = 0f
                volume = 2f
                playAt(loc)
            }
            val dmg = DamageCalculator.calculateMagicDamage(15000, .15f, e.player.stats()!!)
            val radius = 3.0
            var angle = 0f
            val inc = Mth.PI / 20
            task {  }
            while (angle < Mth.PI) {
                angle += inc

                val v = Vector()
                v.x = Mth.cos(angle) * radius
                v.z = Mth.sin(angle) * radius

                v.rotateAroundX(Math.toRadians(loc.pitch.toDouble()))
                v.rotateAroundY(Math.toRadians(-loc.yaw.toDouble()))

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.RED, 1.5f)
                    amount = 3
                    extra = 0f
                    spawnAt(loc.clone().add(v))
                }
                for (entity in loc.clone().add(v).getNearbyLivingEntities(1.0)) {
                    if (entity is Player || entity is ArmorStand)
                        continue
                    entity.macrocosm!!.damage(dmg, e.player.paper!!)
                    entity.velocity = v.clone().multiply(12.0).normalize()
                    DamageHandlers.summonDamageIndicator(entity.location, dmg)
                    e.player.heal(50f, e.player.stats()!!)
                }
            }
        }
    }
}
