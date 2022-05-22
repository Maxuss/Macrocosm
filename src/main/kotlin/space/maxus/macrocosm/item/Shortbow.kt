package space.maxus.macrocosm.item

import org.bukkit.Material
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

open class Shortbow(
    name: String,
    rarity: Rarity,
    extraAbilities: List<ItemAbility> = listOf(),
    stats: Statistics = Statistics.zero(),
    specials: SpecialStatistics = SpecialStatistics(),
    origin: Material = Material.BOW,
    runes: List<ApplicableRune> = listOf()
): AbilityItem(
    ItemType.BOW,
    name,
    rarity,
    origin,
    stats,
    mutableListOf(
        Ability.SHORTBOW_GENERIC.ability,
        *extraAbilities.toTypedArray()
    ),
    specials,
    0,
    runes
)
