package space.maxus.macrocosm.pets.types

import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.SkillTable
import space.maxus.macrocosm.util.id

object WaspPet: Pet(
    id("pet_wasp"),
    "Wasp",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTc0YTM1MzI0MzhmNzQ2NmE2MDI4NDFiZjUxODcxOWNhYjJlNGNlYjk4ODkyZjIyNjAyOTUxNmExOWQwZGFkZCJ9fX0=",
    SkillType.FORAGING,
    listOf(
        PetAbility("Weaponized Honey", "Every <green>10 seconds<gray> shoots a <yellow>stinger<gray> at nearest enemy, dealing <red>[1500] ${Statistic.DAMAGE.display}<gray>."),
        PetAbility("Nest Builder", "Grants you <green>+[0.1] ${Statistic.DEFENSE.display}<gray> for every <gold>${Statistic.FORAGING_FORTUNE.display}<gray> you have."),
        PetAbility("Furious", "Grants you <yellow>+[0.1] ${Statistic.BONUS_ATTACK_SPEED.display}<gray> for every ${Statistic.SPEED.display}<gray> you have over <green>300<gray>.")
    ),
    stats {
        speed = 15f
        health = 50f
        defense = 25f
        foragingFortune = 15f
    }
) {
    override val effects: PetEffects = FixedPetEffects(
        listOf(
            DustPetParticle(0xFFE03D, 1.5f, 2, vec()),
            DustPetParticle(0x000000, 1.5f, 2, vec())
        )
    )
    override val table: LevelingTable = SkillTable

    @EventHandler
    fun nestBuilderAbility(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Weaponized Honey")
        if(!ok)
            return
        val modifier = pet!!.level * .1f
        e.stats.defense += e.stats.foragingFortune * modifier
    }

    @EventHandler
    fun furiousAbility(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Weaponized Honey")
        if(!ok)
            return
        val modifier = pet!!.level * .1f
        if(e.stats.speed >= 300)
            e.stats.attackSpeed += (e.stats.speed - 300) * modifier
    }

    fun init() {
        task(period = 10 * 20L) {
            for((_, player) in Macrocosm.onlinePlayers) {
                val (ok, pet) = ensureRequirement(player, "Weaponized Honey")
                if(!ok)
                    continue
                val nearest =
                    player.paper!!.getNearbyEntities(8.0, 2.0, 8.0).filter { it is LivingEntity && it !is ArmorStand }.firstOrNull() as? LivingEntity
                        ?: continue
                val dmg = DamageCalculator.calculateMagicDamage(1500 * pet!!.level, .2f, player.stats()!!)
                nearest.macrocosm!!.damage(dmg)
                DamageHandlers.summonDamageIndicator(nearest.location, dmg)
                sound(Sound.ENTITY_ARROW_HIT_PLAYER) {
                    volume = 4f
                    playAt(nearest.location)
                }
            }
        }
    }
}
