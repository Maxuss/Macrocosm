package space.maxus.macrocosm.ability.types.item

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.format.TextColor
import net.minecraft.util.Mth
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.Ticker
import space.maxus.macrocosm.util.runNTimes
import space.maxus.macrocosm.util.unreachable
import java.time.Duration
import java.util.*
import kotlin.math.min

object MyspysAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Myspys",
    "Transform yourself into<br><gradient:#DC8F16:#3C2502:#DC8F16>Angel of Hatred<gray> for <red>5 seconds<gray>.<br>In this form you take <red>[0.5:0.2]%<gray> less damage, can fly but can not do physical damage. Instead, each hit on enemy will burst out Hatred Geysers that deal <red>[3500:0.1] ${Statistic.DAMAGE.display}<gray> for <green>2 seconds<gray>."
) {
    override val cost: AbilityCost = AbilityCost(1000, 800, 30)

    private val enabled: MutableList<UUID> = mutableListOf()
    private val fountains: Multimap<UUID, Location> = HashMultimap.create()

    override fun registerListeners() {
        // geysers
        listen<PlayerDealDamageEvent> { e ->
            if (e.isCancelled)
                return@listen

            val p = e.player.paper ?: return@listen
            if (enabled.contains(p.uniqueId)) {
                e.isCancelled = true
                val got = if (fountains.containsKey(p.uniqueId)) fountains.get(p.uniqueId) else null
                if (got != null && got.size >= 3) {
                    fountains.remove(p.uniqueId, got.last())
                }
                summonGeyser(
                    e.damaged.location,
                    e.player,
                    DamageCalculator.calculateMagicDamage(3000, .1f, e.player.stats()!!)
                )
            }
        }
        listen<PlayerReceiveDamageEvent>(priority = EventPriority.LOWEST) { e ->
            if (e.isCancelled)
                return@listen

            if (enabled.contains(e.player.ref)) {
                e.damage *= (1 - min(.75f, DamageCalculator.calculateMagicDamage(.005, .2f, e.player.stats()!!)))
            }
        }
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            enabled.add(e.player.ref)
            val p = e.player.paper ?: return@listen
            p.allowFlight = true

            sound(Sound.ENTITY_PHANTOM_DEATH) {
                pitch = 0f
                volume = 5f

                playAt(p.location)
            }

            var until = 100
            runNTimes(25, 4, {
                sound(Sound.ENTITY_BLAZE_DEATH) {
                    pitch = 0f
                    volume = 5f

                    playAt(p.location)
                }

                enabled.remove(e.player.ref)

                p.allowFlight = false
            }) {
                until -= 4
                val color = when (until) {
                    in 0..10 -> 0xD89400
                    in 10..30 -> 0xBB8307
                    in 30..50 -> 0xC78B06
                    in 50..70 -> 0x2B1E02
                    else -> 0x120D00
                }
                p.title(subText = text("$until").color(TextColor.color(color)), fadeIn = Duration.ofMillis(100))
                renderCircle(p.eyeLocation.add(vec(x = -.23, y = .3, z = -.3)))
            }
        }
    }

    private fun renderCircle(at: Location) {
        async {
            val ticker = Ticker(0 until 5)
            var i = 0f
            while (i < Mth.PI * 2) {
                val tick = ticker.tick()

                val x = Mth.cos(i) * .3
                val z = Mth.sin(i) * .3

                val vecConst = vec(x = x, z = z)

                at.add(vecConst)

                val color = when (tick) {
                    0 -> 0xD89400
                    1 -> 0xBB8307
                    2 -> 0xC78B06
                    3 -> 0x2B1E02
                    4 -> 0x120D00
                    else -> unreachable()
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 1.2f)
                    amount = 1

                    spawnAt(at)
                }

                i += Mth.PI / 4f
            }
        }
    }

    private fun summonGeyser(at: Location, by: MacrocosmPlayer, damage: Float) {
        val ticker = Ticker(0..4)
        val p = by.paper
        fountains.put(by.ref, at)
        runNTimes(7, 5, {
            sound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE) {
                pitch = 0f
                volume = 7f

                playAt(at)
                fountains.remove(by.ref, at)
            }
        }) {
            if (!fountains.containsEntry(by.ref, at)) {
                it.cancel()
                return@runNTimes
            }
            val tick = ticker.tick()

            renderGeyser(at, tick)
            damageGeyser(at, p, damage)
        }
    }

    private fun damageGeyser(at: Location, by: Player?, damage: Float) {
        at.getNearbyLivingEntities(1.0, 6.0) {
            it !is Player && it !is ArmorStand
        }.forEach {
            it.macrocosm?.damage(damage, by)
            DamageHandlers.summonDamageIndicator(at, damage)
        }
    }

    private fun renderGeyser(at: Location, tick: Int) {
        val height = 6
        val (color, secondaryColor) = Pair(color(tick), color(tick + 1))
        val ascent = vec(y = 0.3) // ascend by 3rd of a block each time

        async {
            val y = at.y
            val pos = at.clone()

            // render more particles with slight offset at the start of geyser
            particle(Particle.REDSTONE) {
                data = DustOptions(Color.fromRGB(secondaryColor), 1.3f)
                amount = 6
                extra = 1.4f
                offset = Vector.getRandom()

                spawnAt(at)
            }

            while (pos.y - y <= height) {
                pos.add(ascent)

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 1.6f)
                    amount = 3

                    spawnAt(pos)
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(secondaryColor), 1.3f)
                    amount = 2

                    spawnAt(pos)
                }
            }
        }

        sound(Sound.ENTITY_CREEPER_DEATH) {
            pitch = min(2f, 1.5f + tick / 10f)
            volume = 2f

            playAt(at)
        }
    }

    private fun color(tick: Int): Int {
        return when (tick) {
            0 -> 0xDC8F16
            1 -> 0x1B1000
            2 -> 0x120C01
            3 -> 0xC77B01
            else -> 0x150C00
        }
    }

}
