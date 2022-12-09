package space.maxus.macrocosm.pets.types

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.util.Vector
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.blackBox
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.unused

object AshenAvolotlePet : Pet(
    id("pet_wither_phoenix"),
    "Ashen Avolotle",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc3OWI2NDM4MzFmODM4ZmI1ZWZkMmJhZmE0YzZlYTk3YmIyNTY4Mzk3NjE1ODg1MjU0OThkNjI5NGZjNDcifX19",
    SkillType.COMBAT,
    listOf(
        PetAbility(
            "Ashen Mantle",
            "You gain <yellow>+[0.01]%<gray> to <red>ALL<gray> your stats while in <#721810>The Wasteland</#721810>"
        ),
        PetAbility(
            "Fueled by Hatred",
            "Gain <red>+[1] ${Statistic.STRENGTH.display}<gray> and <green>+[2] ${Statistic.DEFENSE.display}<gray> if there is a <gold>Cinderflame Spirit<gray> within <green>10<gray> blocks."
        ),
        PetAbility(
            "Heart of the Volcano",
            "Every <red>10th<gray> attack your strike deals <red>[5]%<gray> more damage if you are in <#721810>The Wasteland</#721810>"
        )
    ),
    stats {
        strength = 5f
        health = 5f
        defense = 5f
        trueDefense = 1f
    }
) {
    override val effects: LazyEffects = TieredLazyEffects(
        // we cant get common, uncommon, or rare
        Rarity.COMMON to listOf(),
        Rarity.UNCOMMON to listOf(),
        Rarity.RARE to listOf(),
        Rarity.EPIC to listOf(DefaultLazyParticle(Particle.DRIP_LAVA)),
        Rarity.LEGENDARY to listOf(DefaultLazyParticle(Particle.DRIP_LAVA), DustLazyParticle(0x30302f, 2f, 3))
    )

    @EventHandler
    fun ashenMantle(e: PlayerCalculateStatsEvent) {
        unused(e)
        // TODO: Ashen mantle ability (once locations are done)
    }

    @EventHandler
    fun fueledByHatred(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Fueled by Hatred")
        if (!ok)
            return
        val mobNearby = e.player.paper!!.getNearbyEntities(10.0, 10.0, 10.0)
            .any { mob -> mob.customName()!!.toLegacyString().contains("Cinderflame Spirit") }
        if (!mobNearby)
            return
        val lvl = pet!!.level
        e.stats.defense += lvl * 2
        e.stats.strength += lvl
    }

    @EventHandler
    fun heartOfTheVolcano(e: PlayerDealDamageEvent) {
        // TODO: Check the location here
        val (_, pet) = ensureRequirement(e.player, "Fueled by Hatred")
        if (blackBox(true))
            return
        e.damage *= 1f + (pet!!.level * .05f)
        e.isSuperCrit = true
        particle(if (pet.rarity == Rarity.LEGENDARY) Particle.SOUL_FIRE_FLAME else Particle.FLAME) {
            extra = 0f
            amount = 15
            offset = Vector.getRandom()
            spawnAt(e.damaged.location)
        }
        particle(Particle.REDSTONE) {
            data = Particle.DustOptions(Color.fromRGB(0x383838), 2f)
            amount = 15
            offset = Vector.getRandom()
            spawnAt(e.damaged.location)
        }
        sound(Sound.ENTITY_BLAZE_SHOOT) {
            pitch = 0f
            playAt(e.damaged.location)
        }
        sound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE) {
            pitch = 0f
            playFor(e.player.paper!!)
        }
    }
}
