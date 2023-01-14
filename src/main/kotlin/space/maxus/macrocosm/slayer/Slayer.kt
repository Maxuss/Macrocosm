package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.reward.Reward
import space.maxus.macrocosm.slayer.ui.SlayerDrop
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id

internal fun colorFromTier(tier: Int): TextColor = when (tier) {
    1 -> NamedTextColor.GREEN
    2 -> NamedTextColor.YELLOW
    3 -> NamedTextColor.RED
    4 -> NamedTextColor.DARK_RED
    5 -> NamedTextColor.DARK_PURPLE
    6 -> TextColor.color(0x1C77AB)
    else -> NamedTextColor.GOLD
}

fun costFromTier(tier: Int): Double = when (tier) {
    1 -> 500.0
    2 -> 1000.0
    3 -> 5000.0
    4 -> 10000.0
    5 -> 25000.0
    6 -> 250000.0
    else -> 0.0
}

fun rewardExperienceForTier(tier: Int): Double = when (tier) {
    1 -> 5.0
    2 -> 15.0
    3 -> 100.0
    4 -> 500.0
    5 -> 1500.0
    6 -> 5000.0
    else -> 0.0
}

abstract class Slayer(
    val name: String,
    val item: Material,
    val secondaryItem: Material,
    val id: String,
    val description: String,
    val difficulties: List<String>,
    /**
     * These are profession names for high slayer levels (6+).
     *
     * This list is expected to contain only 4 elements
     */
    val professionNames: List<String>,
    val requirementString: String,
    val requirementCheck: (MacrocosmPlayer) -> Boolean,
    val validEntities: List<EntityType>,
    val requiredExp: List<Double>,
    val tiers: IntRange,
    val entities: String,
    val entityKind: String,
    val rewards: List<SlayerReward>,
    drops: List<SlayerDrop>
) : Listener {
    val drops = drops.sortedBy { it.requiredLevel }
    abstract fun abilitiesForTier(tier: Int): List<SlayerAbility>
    abstract fun bossModelForTier(tier: Int): MacrocosmEntity
    abstract fun minisForTier(tier: Int): List<MacrocosmEntity>

    fun bossForTier(tier: Int): SlayerBase {
        return Registry.ENTITY.find(id("${id}_$tier")) as SlayerBase
    }

    fun descript(): List<Component> {
        val reduced = description.reduceToList(25).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlank() }
        return reduced
    }

    companion object {
        fun rewardsOf(vararg rewards: Pair<List<Reward>, RewardDisplay>) =
            rewards.map { SlayerReward(it.second, it.first) }

        val defaultProfessionNames = listOf(
            "Noob", "Novice", "Skilled", "Destroyer", "Bulldozer"
        )
    }
}
