package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.util.Ticker
import java.util.UUID
import kotlin.math.min

object VoidPrismAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Void Tendrils", "Summons <gradient:dark_gray:dark_purple>Tendrils of Void</gradient><gray>, that increase their damage and range over time.<br><yellow>Right Click again to disable<gray>.") {
    override val cost: AbilityCost = AbilityCost(250)

    private val tasks = hashMapOf<UUID, KSpigotRunnable>()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            sound(Sound.ENTITY_PHANTOM_BITE) {
                pitch = 0f
                playAt(e.player.paper!!.location)
            }
            if(tasks.containsKey(e.player.ref)) {
                val task = tasks[e.player.ref]!!
                task.cancel()
                tasks.remove(e.player.ref)
                e.player.sendMessage("<yellow>You have toggled off your Antimatter Ray!")
                return@listen
            }

            // toggling
            val ticker = Ticker(0..10)
            var abs = 1
            e.player.sendMessage("<yellow>You have enabled your Antimatter Ray!")
            tasks[e.player.ref] = task(period = 5L) {
                val p = e.player.paper ?: run {
                    it.cancel()
                    tasks.remove(e.player.ref)
                    return@task
                }
                val ok = ensureRequirements(e.player, EquipmentSlot.HAND)
                if(!ok) {
                    it.cancel()
                    tasks.remove(e.player.ref)
                    return@task
                }
                abs++
                val dmg = DamageCalculator.calculateMagicDamage(min(1000 * abs, 25000), 0.15f, e.player.stats()!!)
                val tick = ticker.tick()
                spawnRay(tick / 13f, p.eyeLocation, colorsFromTick(tick), dmg, p)
                sound(Sound.ENTITY_PHANTOM_HURT) {
                    volume = 1.3f
                    pitch = tick / 20f
                    playAt(p.location)
                }
            }!!
        }
    }

    private fun colorsFromTick(tick: Int): List<Int> {
        return when(tick) {
            in 0..3 -> listOf(0, 0x22013E, 0x22013E, 0)
            in 3..6 -> listOf(0x22013E, 0x3B006D, 0, 0x3B006D)
            in 6..9 -> listOf(0, 0x3B006D, 0x5C07A4, 0)
            10 -> listOf(0xBD98FF, 0xA370FD, 0x4D09C7, 0x3B006D)
            else -> listOf(0x0, 0x0, 0x0, 0x0)
        }
    }

    private fun spawnRay(angle: Float, from: Location, colors: List<Int>, dmg: Float, caster: Player) {
        val direction = from.direction
        var tick = 0
        val mid = from.clone().add(direction.clone().multiply(3.0).normalize())
        val left = mid.clone()
        val right = mid.clone()
        val top = mid.clone()
        val bottom = mid.clone()
        val constDif = direction.clone().multiply(0.3).normalize()
        task(period = 1L) {
            tick++
            if (tick > 10) {
                it.cancel()
                return@task
            }
            val diff = (tick / 10f) * angle
            val y1 = direction.clone().add(vec(y = diff))
            val y2 = direction.clone().add(vec(y = -diff))
            val z1 = direction.clone().rotateAroundY(Math.toRadians(180.0 * diff))
            val z2 = direction.clone().rotateAroundY(Math.toRadians(180.0 * -diff))

            mid.add(constDif)
            top.add(y1.add(constDif))
            bottom.add(y2.add(constDif))
            right.add(z1.add(constDif))
            left.add(z2.add(constDif))

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.fromRGB(colors.random()), 1.7f)
                amount = 2
                extra = 0f
                spawnAt(mid)
            }

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.fromRGB(colors[0]), 1.7f)
                amount = 2
                extra = 0f
                spawnAt(top)
            }

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.fromRGB(colors[1]), 1.7f)
                amount = 2
                extra = 0f
                spawnAt(bottom)
            }

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.fromRGB(colors[2]), 1.7f)
                amount = 2
                extra = 0f
                spawnAt(left)
            }

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.fromRGB(colors[3]), 1.7f)
                amount = 2
                extra = 0f
                spawnAt(right)
            }

            for(entity in mid.getNearbyLivingEntities(1.4 + angle)) {
                if(entity is Player || entity is ArmorStand)
                    continue

                entity.macrocosm!!.damage(dmg, caster)
                DamageHandlers.summonDamageIndicator(entity.location, dmg)
            }
        }
    }
}
