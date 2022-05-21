@file:Suppress("EmptyMethod", "EmptyMethod", "EmptyMethod", "EmptyMethod")

package space.maxus.macrocosm.pets.types

import net.axay.kspigot.extensions.geometry.vec
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.SkillTable
import space.maxus.macrocosm.util.id

object BeePet: Pet(
    id("pet_bee"),
    "Bee",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjU5MDAxYTg1MWJiMWI5ZTljMDVkZTVkNWM2OGIxZWEwZGM4YmQ4NmJhYmYxODhlMGFkZWQ4ZjkxMmMwN2QwZCJ9fX0=",
    SkillType.FARMING,
    listOf(
        PetAbility("Buzzy Bees", "Gain <red>+[0.5] ${Statistic.STRENGTH}<gray> for each <yellow>Bee Pet<gray> owner within <green>10 blocks<gray>."),
        PetAbility("Sweet Honey", "Additionally, regenerate <green>[0.02]%<gray> of your maximum ${Statistic.HEALTH.display}<gray> every second."),
        PetAbility("Bumblebee's Flight", "Increases stats of the <dark_purple>Beekeeper Set<gray> and <gold>The Queen's Stinger<gray> by <red>[0.1]%<gray>.")
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
    override val effects: PetEffects = TieredPetEffects(
        Rarity.COMMON to listOf(DustPetParticle(0xD2D2D1, 0.8f, 3, vec())),
        Rarity.UNCOMMON to listOf(DustPetParticle(0xD2D2D1, 0.8f, 3, vec())),
        Rarity.RARE to listOf(
            DustPetParticle(0xFFE2B4, 0.8f, 2, vec()),
            DustPetParticle(0xF9D59D, 0.8f, 2, vec()),
            ),
        Rarity.EPIC to listOf(
            DustPetParticle(0xFFE2B4, 0.8f, 2, vec()),
            DustPetParticle(0xF9D59D, 0.8f, 2, vec()),
        ),
        Rarity.LEGENDARY to listOf(
            BlockPetParticle(Material.HONEY_BLOCK, 2),
            DustPetParticle(0xF9D59D, 0.8f, 2, vec()),
        )
    )
    override val table: LevelingTable = SkillTable

    @EventHandler
    fun buzzyBeesAbility(e: PlayerCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Buzzy Bees")
        if(!ok)
            return
        val beeOwners = e.player.paper!!.getNearbyEntities(10.0, 10.0, 10.0).filter { it is Player && it.macrocosm!!.activePet?.prototype?.id == id }.size
        e.stats.strength += (pet!!.level * .5f) * beeOwners
    }

    @EventHandler
    fun sweetHoneyAbility(e: PlayerCalculateSpecialStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Sweet Honey")
        if(!ok)
            return
        val maxHealth = e.player.stats()!!.health
        val heal = ((pet!!.level * .02f) * .01f)
        e.stats.extraRegen = maxHealth * heal
    }

    @EventHandler
    fun bumblebeeFlightAbility(e: ItemCalculateStatsEvent) {
        // item checks, etc.
    }
}
