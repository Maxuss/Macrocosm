package space.maxus.macrocosm.pets.types

import org.bukkit.Material
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.generic.id

object TestPet : Pet(
    id("pickle_pet"),
    "Pickle Jar",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODIyMjkyZDkxODNlMzhmM2JlOWYwNmY5NjYzOTRlMmRhZmYzNjJmNzBhZTQ1Y2RlNDEyYjg3YmNkYjg5YzE1OCJ9fX0=",
    SkillType.RUNECRAFTING,
    listOf(
        PetAbility("Pickles", "Deal <red>[10]%<gray> more damage"),
        PetAbility("Pickles?", "Grants <gold>+[50] coins<gray> on kill"),
        PetAbility("Pickles!", "Does... something?")
    ),
    stats {
        strength = 25f
        health = 10f
        ferocity = 10f
    }) {
    override val effects: LazyEffects = FixedLazyEffects(listOf(BlockLazyParticle(Material.SEA_PICKLE)))

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Pickles")
        if (!ok)
            return
        e.damage *= 1 + ((pet!!.level * 10f) / 100f)
    }

    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Pickles?")
        if (!ok)
            return
        val amount = pet!!.level * 50
        e.player.purse += amount.toBigDecimal()
        e.player.sendMessage("<green>Your Pickle Jar granted your <gold>${Formatting.withCommas(amount.toBigDecimal())} coins<green>!")
    }
}
