package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic

object IceConeAbility: AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Ice Cone",
    "All monsters in front of you are frozen and take <red>25,000 ${Statistic.DAMAGE.display}<gray>."
) {
    override val cost: AbilityCost = AbilityCost(500, cooldown = 10)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen

            val damage = DamageCalculator.calculateMagicDamage(25000, .1f, e.player.stats()!!)
            val player = e.player.paper!!
            val dir = player.eyeLocation.direction.normalize()

            spawnHelix(dir, player)

            sound(Sound.BLOCK_LAVA_EXTINGUISH) {
                volume = 1f
                pitch = 0f
                playAt(player.location)
            }

            sound(Sound.BLOCK_GLASS_BREAK) {
                volume = 1f
                pitch = 0f
                playAt(player.location)
            }
            val pos = dir.multiply(2f).relativeLocation(player.location)
            for(entity in pos.getNearbyLivingEntities(4.0)) {
                if(entity is ArmorStand || entity is Player)
                    continue
                val mc = entity.macrocosm!!
                mc.damage(damage, player)
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 50, 4, true, false, false))
                DamageHandlers.summonDamageIndicator(entity.location, damage, DamageType.FROST)
            }
        }
    }

    private var lengthGrow = .05f
    private var angularVelocity = Math.PI / 16
    private var radiusGrow = .006f
    private var particlesCone = 180

    private fun spawnHelix(dir: Vector, player: Player) {
        var step = 0
        val location = player.eyeLocation.add(dir.multiply(2f))
        for (x in 0 until 120) {
            if (step > particlesCone) step = 0
            val angle: Double = step * angularVelocity
            val radius: Float = step * radiusGrow
            val length: Float = step * lengthGrow
            val v = Vector(Mth.cos(angle.toFloat()) * radius.toDouble(), length.toDouble(), Mth.sin(angle.toFloat()) * radius.toDouble())
            v.rotateAroundX((location.pitch + 90) * Mth.DEG_TO_RAD.toDouble())
            v.rotateAroundY(-location.yaw * Mth.DEG_TO_RAD.toDouble())
            location.add(v)

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.WHITE, 1f)
                spawnAt(location)
            }

            location.subtract(v)
            step++
        }
    }
}
