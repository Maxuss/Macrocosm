package space.maxus.macrocosm.item

import org.bukkit.Material
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

open class Shortbow(
    name: String,
    rarity: Rarity,
    extraAbilities: List<MacrocosmAbility> = listOf(),
    stats: Statistics = Statistics.zero(),
    specials: SpecialStatistics = SpecialStatistics(),
    origin: Material = Material.BOW,
    runes: List<RuneSlot> = listOf()
) : AbilityItem(
    ItemType.BOW,
    name,
    rarity,
    origin,
    stats,
    mutableListOf(
        Registry.ABILITY.point(Ability.SHORTBOW_GENERIC.ability.id),
        *extraAbilities.map { Registry.ABILITY.point(it.id) }.toTypedArray()
    ),
    specials,
    0,
    runes,
)
