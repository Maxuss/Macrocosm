package space.maxus.macrocosm.pets.types

import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerDeathEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.SkillTable
import space.maxus.macrocosm.util.id
import java.time.Instant
import java.util.concurrent.TimeUnit

object PhoenixPet: Pet(
    id("pet_phoenix"),
    "Phoenix",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjNhYWY3YjFhNzc4OTQ5Njk2Y2I5OWQ0ZjA0YWQxYWE1MThjZWVlMjU2YzcyZTVlZDY1YmZhNWMyZDg4ZDllIn19fQ==",
    SkillType.COMBAT,
    listOf(
        PetAbility("Eternal", "Lose <green>[1]%<gray> less <gold>Coins<gray> on death."),
        PetAbility("Fiery Rebirth", "On death, <yellow>reborn<gray> with full ${Statistic.HEALTH.display}<gray>, and gain <red>+[2] ${Statistic.STRENGTH.display}<gray> for <green>[0.2]s<gray>.<br><dark_gray>1 Minute Cooldown."),
        PetAbility("Last Wish", "Deal <red>[0.5]% ${Statistic.DAMAGE.display}<gray> when below <gray>25% Health<gray>.")
    ),
    stats {
        strength = 15f
        intelligence = 40f
    }
) {
    override val effects: PetEffects = TieredPetEffects(
        Rarity.EPIC to listOf(DefaultPetParticle(Particle.FLAME, 2)),
        Rarity.LEGENDARY to listOf(
            DefaultPetParticle(Particle.FLAME, 1),
            DustPetParticle(0xDC7217, .4f, 2)
        )
    )
    override val table: LevelingTable = SkillTable

    @EventHandler
    fun eternalAbility(e: PlayerDeathEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Eternal")
        if(!ok)
            return
        e.reduceCoins *= (1f - (pet!!.level / 100f))
    }

    @EventHandler
    fun fieryRebirthAbility(e: PlayerDeathEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Fiery Rebirth")
        if(!ok)
            return
        val abilId = id("pet_fiery_rebirth")
        val cdMillis = TimeUnit.SECONDS.toMillis(60)
        val now = Instant.now().toEpochMilli()
        if(!e.player.lastAbilityUse.contains(abilId)) {
            e.player.lastAbilityUse[abilId] = now
        } else if(e.player.lastAbilityUse[abilId]!! + cdMillis > now) {
            return
        }
        e.player.lastAbilityUse[abilId] = now
        e.isCancelled = true
        e.player.currentHealth = e.player.stats()!!.health
        e.player.tempStats.strength += 2 * pet!!.level

        taskRunLater(4L * pet.level) {
            e.player.tempStats.strength -= 2 * pet.level
        }
    }

    @EventHandler
    fun lastWishAbility(e: PlayerDealDamageEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Last Wish")
        if(!ok)
            return
        val maxHealth = e.player.stats()!!.health
        if(e.player.currentHealth / maxHealth <= .25f) {
            e.damage *= .005f * pet!!.level
        }
    }
}
