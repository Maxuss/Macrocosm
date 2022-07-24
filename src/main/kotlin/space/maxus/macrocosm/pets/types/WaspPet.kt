package space.maxus.macrocosm.pets.types

import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.util.BlockIterator
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.generic.id

object WaspPet : Pet(
    id("pet_wasp"),
    "Wasp",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc0YTM1MzI0MzhmNzQ2NmE2MDI4NDFiZjUxODcxOWNhYjJlNGNlYjk4ODkyZjIyNjAyOTUxNmExOWQwZGFkZCJ9fX0=",
    SkillType.FORAGING,
    listOf(
        PetAbility(
            "Bumblebee's Flight",
            "Increases stats of the <dark_purple>Beekeeper Set<gray> and <gold>The Queen's Stinger<gray> by <red>[0.1]%<gray>."
        ),
        PetAbility(
            "Furious",
            "Grants you <yellow>+[0.1] ${Statistic.BONUS_ATTACK_SPEED.display}<gray> for every ${Statistic.SPEED.display}<gray> you have over <green>300<gray>."
        ),
        PetAbility(
            "Weaponized Honey",
            "Every <green>10 seconds<gray> shoots a <yellow>stinger<gray> at nearest enemy, dealing <red>[1000] ${Statistic.DAMAGE.display}<gray>."
        ),
    ),
    stats {
        speed = 15f
        health = 50f
        defense = 25f
        foragingFortune = 15f
    }
) {
    override val effects: LazyEffects = FixedLazyEffects(
        listOf(
            DustLazyParticle(0xFFE03D, 1.5f, 2, vec()),
            DustLazyParticle(0x000000, 1.5f, 2, vec())
        )
    )

    @EventHandler
    fun bumblebeeFlightAbility(e: ItemCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player ?: return, "Bumblebee's Flight")
        if (!ok)
            return
        val amount = .001f * pet!!.level
        if ((e.item.type.armor && e.item.id.path.contains("beekeeper")) || e.item.id.path.contains("queens_stinger")) {
            e.stats.multiply(1 + amount)
        }
    }

    @EventHandler
    fun furiousAbility(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Furious")
        if (!ok)
            return
        val modifier = pet!!.level * .1f
        if (e.stats.speed >= 300)
            e.stats.attackSpeed += (e.stats.speed - 300) * modifier
    }

    fun init() {
        task(period = 10 * 20L) {
            for ((_, player) in Macrocosm.onlinePlayers) {
                val (ok, pet) = ensureRequirement(player, "Weaponized Honey")
                if (!ok)
                    continue
                val nearest =
                    player.paper!!.getNearbyEntities(8.0, 2.0, 8.0)
                        .firstOrNull { it is LivingEntity && it !is ArmorStand } as? LivingEntity
                        ?: continue
                val dmg = DamageCalculator.calculateMagicDamage(1000 * pet!!.level, .15f, player.stats()!!)
                val bi = BlockIterator(
                    nearest.world,
                    player.paper!!.location.toVector(),
                    nearest.location.toVector().subtract(player.paper!!.location.toVector()).normalize(),
                    1.0,
                    10
                )
                while (bi.hasNext()) {
                    val pos = bi.next()
                    particle(Particle.REDSTONE) {
                        data = DustOptions(Color.YELLOW, 1f)
                        amount = 4
                        offset = vec()
                        spawnAt(pos.location)
                    }
                }
                sound(Sound.ENTITY_BEE_HURT) {
                    volume = 4f
                    pitch = 0f
                    playAt(nearest.location)
                }
                nearest.macrocosm!!.damage(dmg)
                DamageHandlers.summonDamageIndicator(nearest.location, dmg)
            }
        }
    }
}
