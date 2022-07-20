@file:Suppress("EmptyMethod", "EmptyMethod", "EmptyMethod", "EmptyMethod")

package space.maxus.macrocosm.pets.types

import net.axay.kspigot.extensions.geometry.vec
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.generic.id

object BeePet : Pet(
    id("pet_bee"),
    "Bee",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU5MDAxYTg1MWJiMWI5ZTljMDVkZTVkNWM2OGIxZWEwZGM4YmQ4NmJhYmYxODhlMGFkZWQ4ZjkxMmMwN2QwZCJ9fX0=",
    SkillType.FARMING,
    listOf(
        PetAbility(
            "Buzzy Bees",
            "Gain <red>+[0.5] ${Statistic.STRENGTH}<gray> for each <yellow>Bee Pet<gray> owner within <green>10 blocks<gray>."
        ),
        PetAbility(
            "Sweet Honey",
            "Regenerate <green>[0.02]%<gray> of your maximum ${Statistic.HEALTH.display}<gray> every second."
        ),
        PetAbility(
            "Nest Builder",
            "Grants you <green>+[0.1] ${Statistic.DEFENSE.display}<gray> for every <gold>${Statistic.FARMING_FORTUNE.display}<gray> you have."
        ),
    ),
    stats {
        intelligence = 8f
        health = 10f
        defense = 8f
        strength = 3f
        critDamage = 3f
        critChance = 2f
    }
) {
    override val effects: LazyEffects = TieredLazyEffects(
        Rarity.COMMON to listOf(DustLazyParticle(0xD2D2D1, 0.8f, 3, vec())),
        Rarity.UNCOMMON to listOf(DustLazyParticle(0xD2D2D1, 0.8f, 3, vec())),
        Rarity.RARE to listOf(
            DustLazyParticle(0xFFE2B4, 0.8f, 2, vec()),
            DustLazyParticle(0xF9D59D, 0.8f, 2, vec()),
        ),
        Rarity.EPIC to listOf(
            DustLazyParticle(0xFFE2B4, 0.8f, 2, vec()),
            DustLazyParticle(0xF9D59D, 0.8f, 2, vec()),
        ),
        Rarity.LEGENDARY to listOf(
            BlockLazyParticle(Material.HONEY_BLOCK, 2),
            DustLazyParticle(0xF9D59D, 0.8f, 2, vec()),
        )
    )

    @EventHandler
    fun buzzyBeesAbility(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Buzzy Bees")
        if (!ok)
            return
        val beeOwners = e.player.paper!!.getNearbyEntities(10.0, 10.0, 10.0)
            .filter { it is Player && it.macrocosm!!.activePet?.prototype?.id == id }.size
        e.stats.strength += (pet!!.level * .5f) * beeOwners
    }

    @EventHandler
    fun sweetHoneyAbility(e: PlayerCalculateSpecialStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Sweet Honey")
        if (!ok)
            return
        val maxHealth = e.player.stats()!!.health
        val heal = ((pet!!.level * .02f) * .01f)
        e.stats.extraRegen = maxHealth * heal
    }

    @EventHandler
    fun nestBuilderAbility(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Weaponized Honey")
        if (!ok)
            return
        val modifier = pet!!.level * .1f
        e.stats.defense += e.stats.farmingFortune * modifier
    }
}
