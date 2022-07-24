package space.maxus.macrocosm.pets.types

import net.axay.kspigot.extensions.geometry.vec
import org.bukkit.World.Environment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.pets.*
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.generic.id

object EnderDragonPet : Pet(
    id("pet_ender_dragon"),
    "Ender Dragon",
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZjZGFlNTg2YjUyNDAzYjkyYjE4NTdlZTQzMzFiYWM2MzZhZjA4YmFiOTJiYTU3NTBhNTRhODMzMzFhNjM1MyJ9fX0=",
    SkillType.COMBAT,
    listOf(
        PetAbility("Enderian", "Gain <gold>+[0.1]%<gray> on <red>all<gray> stats, when on <dark_purple>The End<gray>."),
        PetAbility(
            "Void Conqueror",
            "Increases stats of <gold>Dragon Sets<gray>, <gold>Void Scepter<gray> and <gold>Aspect of the Dragons<gray> by <red>[.1]%<gray>."
        ),
        PetAbility(
            "Extinction",
            "You deal <red>[2]%<gray> more ${Statistic.DAMAGE.display}<gray>, when there are less than <green>3 enemies<gray> around."
        )
    ),
    stats {
        critDamage = 15f
        strength = 15f
        intelligence = 15f
        magicFind = 3f
    }
) {
    override val effects: LazyEffects = FixedLazyEffects(
        listOf(
            DustLazyParticle(0x6705AD, 1.5f, 2, vec()),
            DustLazyParticle(0x140221, 1.5f, 2, vec())
        )
    )

    @EventHandler
    fun enderianAbility(e: PlayerCalculateSpecialStatsEvent) {
        if (e.player.paper?.world?.environment != Environment.THE_END)
            return
        val (ok, pet) = ensureRequirement(e.player, "Enderian")
        if (!ok)
            return

        e.stats.statBoost += .001f * pet!!.level
    }

    @EventHandler
    fun voidConquerorAbility(e: ItemCalculateStatsEvent) {
        val (ok, pet) = ensureRequirement(e.player ?: return, "Void Conqueror")
        if (!ok)
            return
        val amount = .001f * pet!!.level
        if ((e.item.type.armor && e.item.id.path.contains("dragon")) || e.item.id.path.contains("void_scepter") || e.item.id.path.contains(
                "aspect_of_the_dragons"
            )
        ) {
            e.stats.multiply(1 + amount)
        }
    }

    @EventHandler
    fun extinctionAbility(e: PlayerDealDamageEvent) {
        val (ok, pet) = ensureRequirement(e.player, "Extinction")
        if (!ok)
            return

        if (e.player.paper!!.getNearbyEntities(8.0, 8.0, 8.0)
                .filter { it is LivingEntity && it !is ArmorStand }.size <= 3
        ) {
            e.damage *= (.02f * pet!!.level)
        }
    }
}
