package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
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

object AOTDAbility: AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Dragon Rage",
    "All monsters in front of you take <red>6,000 ${Statistic.DAMAGE.display}<gray> and massive knockback."
) {
    override val cost: AbilityCost = AbilityCost(200, cooldown = 2)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val damage = DamageCalculator.calculateMagicDamage(6000, .2f, e.player.stats()!!)
            val player = e.player.paper!!
            val dir = player.eyeLocation.direction.normalize()

            spawnHelix(dir, player)

            sound(Sound.ENTITY_ENDER_DRAGON_GROWL) {
                volume = 1f
                playAt(player.location)
            }
            val pos = dir.multiply(3f).relativeLocation(player.location)
            val specs = e.player.specialStats()!!
            for(entity in pos.getNearbyLivingEntities(4.0)) {
                if(entity is ArmorStand || entity is Player)
                    continue
                val mc = entity.macrocosm!!
                mc.damage(damage, player)
                DamageHandlers.summonDamageIndicator(entity.location, damage)
                val knockbackAmount = 1.8 * (1 + specs.knockbackBoost) * (1 - mc.specialStats().knockbackResistance)
                val nmsDamaged = (entity as CraftLivingEntity).handle
                val nmsDamager = (player as CraftLivingEntity).handle
                nmsDamaged.knockback(
                    knockbackAmount,
                    Mth.sin(nmsDamager.getYRot() * 0.017453292F).toDouble(),
                    -Mth.cos(nmsDamager.getYRot() * 0.017453292F).toDouble(),
                    nmsDamager
                )
                nmsDamager.deltaMovement = nmsDamager.deltaMovement.multiply(.6, 1.0, 0.6)
            }
        }
    }

    private var lengthGrow = .05f
    private var angularVelocity = Math.PI / 16
    private var radiusGrow = .01f
    private var particlesCone = 180

    private fun spawnHelix(dir: Vector, player: Player) {
        var step = 0
        val location = player.eyeLocation.add(dir.multiply(2f))
        for (x in 0 until 60) {
            task(delay = 5L + x / 10) {
                if (step > particlesCone) step = 0
                val angle: Double = step * angularVelocity
                val radius: Float = step * radiusGrow
                val length: Float = step * lengthGrow
                val v = Vector(Mth.cos(angle.toFloat()) * radius.toDouble(), length.toDouble(), Mth.sin(angle.toFloat()) * radius.toDouble())
                v.rotateAroundX((location.pitch + 90) * Mth.DEG_TO_RAD.toDouble())
                v.rotateAroundY(-location.yaw * Mth.DEG_TO_RAD.toDouble())
                location.add(v)

                particle(Particle.FLAME) {
                    extra = 0f
                    spawnAt(location)
                }

                location.subtract(v)
                step++
            }
        }
    }
}
