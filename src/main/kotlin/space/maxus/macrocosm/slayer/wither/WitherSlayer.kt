package space.maxus.macrocosm.slayer.wither

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.util.todo

object WitherSlayer : Slayer(
    "<gold>Cinderflame Spirit",
    Material.CHARCOAL,
    Material.NETHERITE_SCRAP,
    "cinderflame_spirit",
    "A spirit born from the devastating flames of Nether and was never supposed to exist.",
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
    rewardsOf(
        // todo: rewards
    ),
    listOf(
        // todo: drops
    )
) {
    override fun abilitiesForTier(tier: Int): List<SlayerAbility> {
        todo()
    }

    override fun bossModelForTier(tier: Int): MacrocosmEntity {
        todo()
    }

    override fun minisForTier(tier: Int): List<MacrocosmEntity> {
        todo()
    }
}
