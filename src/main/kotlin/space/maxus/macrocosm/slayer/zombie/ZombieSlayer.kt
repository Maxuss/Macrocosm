package space.maxus.macrocosm.slayer.zombie

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.stats.stats

object ZombieSlayer: Slayer(
    "<red>Revenant Horror",
    Material.ROTTEN_FLESH,
    "revenant_horror",
    "Abhorrent zombie, that has been sealed in it's tomb for long time.",
    listOf(EntityType.ZOMBIE),
    listOf(
        100.0,
        800.0,
        2500.0,
        12000.0,
        25000.0,
        40000.0
    ),
    1..6
) {
    override fun abilitiesForTier(tier: Int): List<SlayerAbility> {
        return listOf(ZombieAbilities.REGENERATION)
    }

    override fun bossForTier(tier: Int): MacrocosmEntity {
        return DefaultRevenant(stats {
            health = 25000f
            defense = 150f
            damage = 150f
            strength = 200f
            speed = 250f
        }, LootPool.of(), 1000.0)
    }

    override fun minisForTier(tier: Int): List<MacrocosmEntity> {
        return listOf()
    }
}
