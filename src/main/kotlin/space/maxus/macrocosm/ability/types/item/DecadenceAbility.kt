package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Color
import org.bukkit.Location
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
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.runNTimes
import space.maxus.macrocosm.util.unreachable
import kotlin.random.Random

object DecadenceAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Decadence", "Shoot a chunk of <gradient:#AB6C08:#B77A1A:#291A01>Hatred Energy</gradient> that explodes dealing <red>[5000:0.15] ${Statistic.DAMAGE.display}<gray> and healing for <red>1%<gray> of damage dealt.") {
    override val cost: AbilityCost = AbilityCost(300, 150, 2)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val player = e.player
            val p = player.paper!!

            val base = p.eyeLocation
            val dir = base.direction.multiply(4f).normalize().multiply(1.3f)
            sound(Sound.ENTITY_GHAST_WARN) {
                playAt(base)
            }
            runNTimes(4, 2, {
                val amount = explodeBall(player, base)
                player.sendMessage("<gray>Your <red>Decadence<gray> hit nearby entities for <red>${Formatting.withCommas(amount.toBigDecimal())}<gray> damage!")
                player.heal(amount * .01f)
                sound(Sound.ENTITY_GHAST_HURT) {
                    pitch = 0f
                    volume = 3f
                    playAt(base)
                }
            }) {
                base.add(dir)

                renderBall(base)
            }
        }
    }

    fun explodeBall(player: MacrocosmPlayer, pos: Location): Float {
        async {
            for (i in 0..20) {
                val color = when (Random.nextInt(0, 6)) {
                    0 -> 0xD67E0A
                    1 -> 0xB76D0B
                    2 -> 0x985907
                    3 -> 0xB96B05
                    4 -> 0x8A5106
                    5 -> 0xFFFFFF
                    else -> unreachable()
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 2f)
                    offset = Vector.getRandom()
                    amount = 5
                    extra = 1.2f

                    spawnAt(pos)
                }
            }
        }

        val damage = DamageCalculator.calculateMagicDamage(5000, .15f, player.stats()!!)
        val paper = player.paper ?: return 0f
        var total = 0f
        for(entity in pos.getNearbyLivingEntities(4.0)) {
            if(entity is ArmorStand || entity is Player)
                continue

            total += damage
            entity.macrocosm?.damage(damage, paper)
        }
        return total
    }

    fun renderBall(pos: Location) {
        var i = 0f
        val at = pos.clone()
        while(i < Mth.PI) {
            val radius = Mth.sin(i) * 0.2
            val y = Mth.cos(i) * 0.2

            var a = 0f
            while(a < Mth.PI * 2) {
                val x = Mth.cos(a) * radius
                val z = Mth.sin(a) * radius

                val vecConst = vec(x, y, z)

                at.add(vecConst)

                val color = when(Random.nextInt(0, 5)) {
                    0 -> 0x201914
                    1 -> 0x362415
                    2 -> 0x47341B
                    3 -> 0x3B341F
                    4 -> 0x322724
                    else -> unreachable()
                }
                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 1.2f)
                    amount = 2

                    spawnAt(at.clone().add(vec(x - .05, y - .05, z - 0.5)))
                }

                at.subtract(vecConst)

                a += Mth.PI / 5f
            }

            i += Mth.PI / 5f
        }
    }
}
