package space.maxus.macrocosm.slayer.zombie

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.loot.DropRarity
import space.maxus.macrocosm.reward.FixedStatReward
import space.maxus.macrocosm.reward.ItemReward
import space.maxus.macrocosm.reward.RecipeReward
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.MaterialDisplay
import space.maxus.macrocosm.slayer.SkullDisplay
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.slayer.ui.SlayerDrop
import space.maxus.macrocosm.slayer.ui.visual
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.generic.id
import space.maxus.macrocosm.util.unreachable

object ZombieSlayer : Slayer(
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
        listOf(
            FixedStatReward(Statistic.HEALTH, 2f),
            RecipeReward(id("wand_of_healing"))
        ) to MaterialDisplay(Material.ROTTEN_FLESH),
        listOf(
            FixedStatReward(Statistic.DEFENSE, 3f),
            RecipeReward(id("revenant_viscera")),
            RecipeReward(id("undead_sword"))
        ) to MaterialDisplay(Material.COOKED_PORKCHOP),
        listOf(
            FixedStatReward(Statistic.HEALTH, 3f), RecipeReward(id("revenant_falchion")), RecipeReward(
                id("wand_of_mending")
            ), RecipeReward(id("undead_heart")), ItemReward(id("revenant_catalyst"))
        ) to MaterialDisplay(Material.STICK),
        listOf(
            FixedStatReward(Statistic.DEFENSE, 4f),
            RecipeReward(id("revenant_leggings")),
            RecipeReward(id("revenant_boots")),
            RecipeReward(
                id("rancorous_staff")
            )
        ) to MaterialDisplay(Material.DIAMOND_BOOTS),
        listOf(
            FixedStatReward(Statistic.HEALTH, 5f),
            RecipeReward(id("master_necromancer_boots")),
            RecipeReward(id("crystallized_heart")),
            RecipeReward(
                id("revenant_chestplate")
            ),
            RecipeReward(id("master_necromancer_boots")),
            RecipeReward(id("voodoo_doll"))
        ) to SkullDisplay("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY0ZjI1Y2ZmZjc1NGYyODdhOTgzOGQ4ZWZlMDM5OTgwNzNjMjJkZjdhOWQzMDI1YzQyNWUzZWQ3ZmY1MmMyMCJ9fX0="),
        listOf(
            FixedStatReward(Statistic.FEROCITY, 1f),
            RecipeReward(id("wand_of_restoration")),
            RecipeReward(id("reaper_falchion")),
            RecipeReward(
                id("revived_heart")
            ),
            ItemReward(
                id("reaper_catalyst")
            ),
            RecipeReward(id("master_necromancer_leggings"))
        ) to MaterialDisplay(Material.DIAMOND_SWORD),
        listOf(
            FixedStatReward(Statistic.HEALTH, 5f),
            RecipeReward(id("master_necromancer_chestplate")),
            RecipeReward(id("reaper_mask")),
            RecipeReward(
                id("reaper_boots")
            ),
            RecipeReward(
                id("reaper_leggings")
            ),
            RecipeReward(id("reaper_chestplate")),
            RecipeReward(id("revenant_innards"))
        ) to MaterialDisplay(Material.LEATHER_CHESTPLATE),
        listOf(
            FixedStatReward(Statistic.HEALTH, 6f),
            RecipeReward(id("axe_of_the_shredded")),
            RecipeReward(id("reaper_scythe")),
            RecipeReward(
                id("wardens_helmet")
            ),
            RecipeReward(
                id("entombed_mask")
            ),
            RecipeReward(id("master_necromancer_helmet"))
        ) to SkullDisplay("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlYjBiZDg1YWFkZGYwZDI5ZWQwODJlYWMwM2ZjYWRlNDNkMGVlODAzYjBlODE2MmFkZDI4YTYzNzlmYjU0ZSJ9fX0="),
        listOf(
            FixedStatReward(Statistic.HEALTH, 7f),
            RecipeReward(id("reaper_gem"))
        ) to SkullDisplay("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjMxNzUyODZjZDNiYTFhM2E5YzkwODI5NzdkMDlkZDM3YjE3N2FiZjM3YTQ2NjU4MGMyN2QxZGVlNzJiM2MxOCJ9fX0="),
    ),
    listOf(
        SlayerDrop(
            visual("revenant_flesh", DropRarity.COMMON, 1.0), 1, 0, hashMapOf(
                1 to 1..3, 2 to 9..18, 3 to 30..50, 4 to 50..60, 5 to 58..64, 6 to 65..66
            )
        ),
        SlayerDrop(
            visual("foul_flesh", DropRarity.RARE, 0.2), 2, 1, hashMapOf(
                2 to 1..1, 3 to 2..4, 4 to 4..5, 5 to 4..6, 6 to 0..0
            )
        ),
        SlayerDrop(
            visual("rancid_flesh", DropRarity.VERY_RARE, 0.1), 6, 8, hashMapOf(
                6 to 4..6
            )
        ),
        SlayerDrop(
            visual("revenant_viscera", DropRarity.VERY_RARE, 0.05), 5, 7, hashMapOf(
                5 to 1..2, 6 to 3..4
            )
        ),
        SlayerDrop(
            visual("revenant_catalyst", DropRarity.SUPER_RARE, 0.01), 2, 2, hashMapOf(
                2 to 1..1, 3 to 1..1, 4 to 1..1, 5 to 1..1
            )
        ),
        SlayerDrop(
            visual("reaper_catalyst", DropRarity.SUPER_RARE, 0.01), 3, 4, hashMapOf(
                3 to 1..1, 4 to 1..1, 5 to 1..1
            )
        ),
        SlayerDrop(
            visual("beheaded_horror", DropRarity.CRAZY_RARE, 1 / 521.0), 3, 5, hashMapOf(
                3 to 1..1, 4 to 1..1, 5 to 1..1
            )
        ),
        SlayerDrop(
            visual("decaying_brain", DropRarity.CRAZY_RARE, 1 / 666.0), 5, 6, hashMapOf(
                5 to 1..1, 6 to 1..1
            )
        ),
        SlayerDrop(
            visual("enchanted_diamond", DropRarity.VERY_RARE, 0.05), 4, 5, hashMapOf(
                4 to 1..3, 5 to 1..4
            )
        ),
        SlayerDrop(
            visual("forbidden_scrolls", DropRarity.SUPER_RARE, 1 / 200.0), 5, 7, hashMapOf(
                5 to 1..1, 6 to 0..0
            )
        ),
        SlayerDrop(
            visual("scythe_blade", DropRarity.CRAZY_RARE, 1 / 969.0), 4, 6, hashMapOf(
                4 to 1..1, 5 to 1..1, 6 to 1..1,
            )
        ),
        SlayerDrop(
            visual("diamond_rune_legendary", DropRarity.CRAZY_RARE, 1 / 777.0), 4, 5, hashMapOf(
                4 to 1..1, 5 to 1..1, 6 to 1..2
            )
        ),
        SlayerDrop(
            visual("emerald_rune_legendary", DropRarity.CRAZY_RARE, 1 / 777.0), 6, 7, hashMapOf(
                6 to 1..1
            )
        ),
        SlayerDrop(
            visual("enchanted_netherite_scrap", DropRarity.VERY_RARE, 0.05), 6, 7, hashMapOf(
                6 to 1..4
            )
        ),
        SlayerDrop(
            visual("wardens_heart", DropRarity.INSANE, 1 / 5_000.0), 5, 8, hashMapOf(
                5 to 1..1, 6 to 1..1
            )
        ),
        SlayerDrop(
            visual("raging_essence", DropRarity.UNBELIEVABLE, 1 / 4_000.0), 6, 8, hashMapOf(
                6 to 1..1
            )
        )
    )
) {
    override fun abilitiesForTier(tier: Int): List<SlayerAbility> {
        return when (tier) {
            1 -> listOf(ZombieAbilities.REGENERATION)
            2 -> listOf(ZombieAbilities.REGENERATION, ZombieAbilities.CRUMBLING_TOUCH)
            6 -> listOf(
                ZombieAbilities.CONSTANT_FEAR,
                ZombieAbilities.IMPENDING_DOOM,
                ZombieAbilities.DOOMSTONE,
                ZombieAbilities.MEAT_SKEWER,
                ZombieAbilities.EXANIMATED_REPEL
            )
            else -> listOf()
        }
    }

    override fun bossModelForTier(tier: Int): MacrocosmEntity {
        return when (tier) {
            1 -> RevenantHorror(
                stats {
                    health = 500f
                    defense = 200f
                    damage = 10f
                    strength = 25f
                    speed = 200f
                },
                1,
                85.0
            )
            2 -> RevenantHorror(
                stats {
                    health = 25000f
                    defense = 400f
                    damage = 50f
                    strength = 50f
                    speed = 250f
                },
                2,
                200.0
            )
            3 -> RevenantHorror(
                stats {
                    health = 150_000f
                    defense = 600f
                    trueDefense = 250f
                    damage = 100f
                    strength = 120f
                    speed = 250f
                },
                3,
                560.0
            )
            4 -> RevenantHorror(
                stats {
                    health = 1_200_000f
                    defense = 800f
                    trueDefense = 300f
                    damage = 150f
                    strength = 200f
                    speed = 275f
                },
                4,
                1200.0
            )
            5 -> AtonedHorror
            6 -> EntombedReaper
            else -> unreachable()
        }
    }

    override fun minisForTier(tier: Int): List<MacrocosmEntity> {
        return listOf()
    }
}
