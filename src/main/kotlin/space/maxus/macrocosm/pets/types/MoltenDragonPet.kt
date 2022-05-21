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
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.SkillTable
import space.maxus.macrocosm.util.id
import kotlin.math.roundToInt

object MoltenDragonPet: Pet(
    id("pet_molten_dragon"),
    "Molten Dragon",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg0NTg3NTg4ODg5NmFiNmI2ZTVmMjlkNjZhYzllZjZiNDFmMjI3ZTEyNjg1ZGU0Y2IxMzQ5ZTMwYzBlMzVjOCJ9fX0=",
    SkillType.MINING,
    listOf(
        PetAbility("Molten Fury", "Deal <red>+[1]% ${Statistic.DAMAGE.display}<gray> when on fire."),
        PetAbility("Sleeping Wrath", "Boosts <gold>all<gray> your stats by <red>[0.1]%<gray> while standing still."),
        PetAbility("Profaned Rage", "Every <green>10 seconds<gray>, cast a <gold>Fire Storm<gray>, dealing <red>[100]%<gray> of your ${Statistic.STRENGTH.display}<gray> to all nearby enemies."),
        ),
    stats {
        strength = 30f
        critDamage = 10f
        ferocity = 15f
        attackSpeed = 10f
        intelligence = 5f
    }
) {
    override val effects: PetEffects = TieredPetEffects(
        Rarity.EPIC to listOf(
            DustPetParticle(0x403F40, 2f, 2, vec()),
            DustPetParticle(0xDC6500, 1.5f, 3, vec(-.5f))
        ),
        Rarity.LEGENDARY to listOf(
            DustPetParticle(0x403F40, 2f, 1, vec()),
            DustPetParticle(0xDC6500, 1.5f, 2, vec(-.5f)),
            DefaultPetParticle(Particle.DRIP_LAVA, 1, vec())
        )
    )
    override val table: LevelingTable = SkillTable

    @EventHandler
    fun moltenFuryAbility(e: PlayerDealDamageEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Molten Fury")
        if(!ok)
            return
        if(e.player.paper!!.fireTicks <= 0)
            return
        e.damage *= (.01f * pet!!.level)
        sound(Sound.ITEM_FIRECHARGE_USE) {
            pitch = 0f
            playAt(e.player.paper!!.location)
        }
    }

    @EventHandler
    fun sleepingWrathAbility(e: PlayerCalculateSpecialStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Molten Fury")
        if(!ok)
            return
        val len = e.player.paper!!.velocity.length()
        if(len > 0.13f)
            return
        e.stats.statBoost += (.001f * pet!!.level)
    }

    fun init() {
        task(period = 10L * 20L) {
            for((_, player) in Macrocosm.onlinePlayers) {
                val (ok, pet) = ensureRequirement(player, "Profaned Rage")
                if(!ok)
                    continue
                val dmg = DamageCalculator.calculateMagicDamage((pet!!.level * player.stats()!!.intelligence).roundToInt(), .01f, player.stats()!!)

                val nearest = player.paper!!.getNearbyEntities(10.0, 2.0, 10.0)
                    .firstOrNull { it is LivingEntity && it !is ArmorStand && it !is Player } as? LivingEntity
                if(nearest != null) {
                    val iter = BlockIterator(
                        nearest.world,
                        player.paper!!.location.toVector(),
                        player.paper!!.location.toVector().subtract(nearest.location.toVector()).normalize().multiply(-1f),
                        1.0,
                        10
                    )
                    var iteration = 1
                    while (iter.hasNext()) {
                        val next = iter.next()
                        iteration++
                        particle(Particle.FLAME) {
                            extra = 0f
                            amount = 15
                            offset = Vector.getRandom()
                            spawnAt(next.location)
                        }
                        particle(Particle.REDSTONE) {
                            data = DustOptions(Color.fromRGB(0x383838), 2f)
                            amount = 15
                            offset = Vector.getRandom()
                            spawnAt(next.location)
                        }
                        for (entity in next.location.getNearbyLivingEntities(3.0)) {
                            if (entity !is ArmorStand && entity !is Player) {
                                entity.macrocosm!!.damage(dmg, player.paper)
                                DamageHandlers.summonDamageIndicator(entity.location, dmg, DamageType.FIRE)
                            }
                        }
                        if(iteration >= 10)
                            break
                    }
                    sound(Sound.ENTITY_BLAZE_SHOOT) {
                        pitch = 0f
                        playAt(player.paper!!.location)
                    }
                    sound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE) {
                        pitch = 0f
                        playFor(player.paper!!)
                    }
                }
            }
        }
    }
}
