package space.maxus.macrocosm.enchants

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

class SimpleEnchantment(
    name: String,
    description: String,
    levels: IntRange,
    applicable: List<ItemType>,
    base: Statistics = Statistics.zero(),
    special: SpecialStatistics = SpecialStatistics(),
    multiplier: Float = 1f,
    conflicts: List<String> = listOf()
): EnchantmentBase(
    name,
    description,
    levels,
    applicable,
    base,
    special,
    multiplier,
    conflicts
)
