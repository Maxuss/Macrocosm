package space.maxus.macrocosm.slayer.wither

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.unreachable

object WitherSlayer : Slayer(
    "<gold>Cinderflame Spirit",
    Material.CHARCOAL,
    Material.NETHERITE_SCRAP,
    "cinderflame_spirit",
    "A spirit born from the devastating flames of The Sizzling Island and was never supposed to exist.",
    listOf(
        "Challenging",
        "Hard",
        "Fierce",
        "Insane",
        "Scorching",
        "Mind Melting"
    ),
    listOf(
        "Extinguisher",
        "Frost Bringer",
        "Eradicator",
        "Scalding Soul"
    ),
    "<gold>Requires <red>☠ Revenant Horror 5<gold>",
    { player ->
        (player.slayers[SlayerType.REVENANT_HORROR]?.level ?: -1) >= 5
    },
    listOf(EntityType.WITHER_SKELETON),
    listOf(
        500.0,
        1000.0,
        3000.0,
        20000.0,
        40000.0,
        60000.0
    ),
    1..6,
    "Wither Skeletons",
    "Wither Skeleton",
    rewardsOf(
        // todo: rewards
    ),
    listOf(
        // todo: drops
    )
) {
    override fun abilitiesForTier(tier: Int): List<SlayerAbility> {
        return when(tier) {
            1 -> listOf(WitherAbilities.CAUTERIZE)
            2 -> listOf(WitherAbilities.CAUTERIZE, WitherAbilities.CINDER_BARRIER)
            3 -> listOf(WitherAbilities.CAUTERIZE, WitherAbilities.CINDER_BARRIER, WitherAbilities.INFERNO_BURST)
            4 -> listOf(WitherAbilities.CAUTERIZE, WitherAbilities.CINDER_BARRIER, WitherAbilities.INFERNO_BURST, WitherAbilities.UNDERWORLD_UNSEALED)
            else -> listOf()
        }
    }

    override fun bossModelForTier(tier: Int): MacrocosmEntity {
        return when(tier) {
            1 -> CinderflameSpirit(
                stats {
                    health = 15000f
                    defense = 300f
                    damage = 50f
                    strength = 50f
                    speed = 150f
                },
                1,
                500.0
            )
            2 -> CinderflameSpirit(
                stats {
                    health = 50000f
                    defense = 500f
                    damage = 150f
                    strength = 100f
                    speed = 150f
                },
                2,
                800.0
            )
            3 -> CinderflameSpirit(
                stats {
                    health = 250_000f
                    defense = 700f
                    damage = 150f
                    strength = 150f
                    speed = 200f
                    ferocity = 10f
                    trueDefense = 250f
                },
                3,
                1200.0
            )
            4 -> CinderflameSpirit(
                stats {
                    health = 1_900_000f
                    defense = 900f
                    damage = 200f
                    strength = 200f
                    speed = 230f
                    ferocity = 15f
                    trueDefense = 250f
                },
                4,
                2300.0
            )
            5 -> IncendiaryIncarnation
            6 -> PyroclasticGoliath
            else -> unreachable()
        }
    }

    override fun minisForTier(tier: Int): List<MacrocosmEntity> {
        return listOf()
    }
}
