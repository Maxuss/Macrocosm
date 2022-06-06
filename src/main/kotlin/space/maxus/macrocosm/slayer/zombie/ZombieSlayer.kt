package space.maxus.macrocosm.slayer.zombie

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.loot.DropRarity
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.reward.Reward
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.MaterialDisplay
import space.maxus.macrocosm.slayer.SkullDisplay
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.slayer.ui.SlayerDrop
import space.maxus.macrocosm.slayer.ui.visual
import space.maxus.macrocosm.stats.stats

object ZombieSlayer: Slayer(
    "<red>Revenant Horror",
    Material.ROTTEN_FLESH,
    Material.DIAMOND,
    "revenant_horror",
    "Abhorrent zombie, that has been sealed in it's tomb for long time.",
    listOf(
        "Easy",
        "Trivial",
        "Challenging",
        "Hard",
        "Insane",
        "Excruciating"
    ),
    listOf(
        "Savage",
        "Deathripper",
        "Eradicator",
        "Grim Reaper"
    ),
    "<dark_aqua>Requires Combat LVL 10<gray>.",
    { player ->
        player.skills.level(SkillType.COMBAT) >= 10
    },
    listOf(EntityType.ZOMBIE),
    listOf(
        100.0,
        800.0,
        2500.0,
        12000.0,
        25000.0,
        40000.0
    ),
    1..6,
    "Zombies",
    rewardsOf(
        Reward.repeating(2) to MaterialDisplay(Material.ROTTEN_FLESH),
        Reward.repeating(2) to MaterialDisplay(Material.PORKCHOP),
        Reward.repeating(2) to MaterialDisplay(Material.STICK),
        Reward.repeating(2) to MaterialDisplay(Material.DIAMOND_CHESTPLATE),
        Reward.repeating(2) to SkullDisplay("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY0ZjI1Y2ZmZjc1NGYyODdhOTgzOGQ4ZWZlMDM5OTgwNzNjMjJkZjdhOWQzMDI1YzQyNWUzZWQ3ZmY1MmMyMCJ9fX0="),
        Reward.repeating(2) to MaterialDisplay(Material.DIAMOND_SWORD),
        Reward.repeating(2) to MaterialDisplay(Material.LEATHER_CHESTPLATE),
        Reward.repeating(2) to SkullDisplay("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlYjBiZDg1YWFkZGYwZDI5ZWQwODJlYWMwM2ZjYWRlNDNkMGVlODAzYjBlODE2MmFkZDI4YTYzNzlmYjU0ZSJ9fX0="),
        Reward.repeating(3) to SkullDisplay("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTYwMzgzYTI5YWQ4YjdiYWViNGMwYzdlMTVmMzUyMGYwN2VjNzU2NWY1YWY4NDFhNmY4MTJhYTQxOWJiNiJ9fX0="),
    ),
    listOf(
        SlayerDrop(visual("revenant_flesh", DropRarity.COMMON, 1.0), 1, 0, listOf(
            1..3, 9..18, 30..50, 50..60, 58..64, 65..66
        ))
    )
) {
    override fun abilitiesForTier(tier: Int): List<SlayerAbility> {
        return when(tier) {
            1 -> listOf(ZombieAbilities.REGENERATION)
            2 -> listOf(ZombieAbilities.REGENERATION, ZombieAbilities.CRUMBLING_TOUCH)
            else -> listOf()
        }
    }

    override fun bossModelForTier(tier: Int): MacrocosmEntity {
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
