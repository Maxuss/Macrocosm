package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.min

object OrderSetAbility : FullSetBonus(
    "Blessing of Order",
    "Constructs an <green>aura<gray> around you, that boosts ${Statistic.VITALITY.display}<gray> by <green>75%<gray> for all players within <yellow>8 blocks<gray>.<br>For every player within <yellow>10 blocks<gray> you gain <red>+1.5% ${Statistic.HEALTH.display}<gray>.${
        exemptsEssence(
            "<green>Devotedly Good"
        )
    }"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            val mc = e.player
            val paper = mc.paper ?: return@listen
            var mod = 1f
            paper.getNearbyEntities(8.0, 8.0, 8.0).filterIsInstance<Player>().forEach { nearby ->
                if (ensureSetRequirement(nearby.macrocosm ?: return@forEach)) {
                    mod += .75f
                    particle(Particle.VILLAGER_HAPPY) {
                        amount = 8
                        spawnAt(nearby.eyeLocation)
                    }
                }
            }
            e.stats.vitality *= mod
        }
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen

            val paper = e.player.paper ?: return@listen
            val mod = 1 + (paper.getNearbyEntities(10.0, 10.0, 10.0).filterIsInstance<Player>().size * 0.015f)
            e.stats.vitality *= mod
        }
    }
}

object EarthPolaritySetBonus : FullSetBonus(
    "Blessing of Earth",
    "You take <green>30%<gray> less ${Statistic.DAMAGE.display}<gray> while standing <yellow>on ground<gray> but you deal <red>70%<gray> less ranged ${Statistic.DAMAGE.display}<gray>.${
        exemptsEssence(
            "<green>Lawful <yellow>Neutral"
        )
    }"
) {
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            val p = e.player.paper!!
            val onGround = !p.location.add(vec(y = -1)).block.type.isAir
            if (onGround) {
                sound(Sound.ENTITY_PLAYER_BURP) {
                    pitch = 0f
                    volume = 0.5f
                    playAt(p.location)
                }
                particle(Particle.BLOCK_CRACK) {
                    data = Material.DIRT.createBlockData()
                    amount = 15
                    offset = Vector.getRandom()
                    spawnAt(p.location)
                }
                e.damage *= 0.7f
            }
        }
        listen<PlayerDealDamageEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            if (e.kind == DamageKind.RANGED)
                e.damage *= 0.3f
        }
    }
}

object FirePolaritySetBonus : FullSetBonus(
    "Blessing of Fire",
    "You deal <red>+50% Melee ${Statistic.DAMAGE.display}<gray> but your ${Statistic.INTELLIGENCE.display}<gray> is capped at <red>25%<gray> of your total ${Statistic.STRENGTH.display}<gray>.${
        exemptsEssence(
            "<green>Lawful <red>Evil"
        )
    }"
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (e.kind != DamageKind.MELEE || !ensureSetRequirement(e.player))
                return@listen
            e.damage *= 1.5f
            particle(Particle.REDSTONE) {
                amount = 14
                data = DustOptions(Color.fromRGB(0xFF6A29), 1.7f)
                offset = Vector.getRandom()
                spawnAt(e.player.paper!!.eyeLocation.add(vec(y = 0.3)))
            }
            sound(Sound.ENTITY_BLAZE_BURN) {
                pitch = 0f
                volume = 1.3f
                playAt(e.player.paper!!.location)
            }
        }
        listen<PlayerCalculateStatsEvent>(priority = EventPriority.LOWEST) { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.intelligence = min(e.stats.strength * .25f, e.stats.intelligence)
        }
    }
}
