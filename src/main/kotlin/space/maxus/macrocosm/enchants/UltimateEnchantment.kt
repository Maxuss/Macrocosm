package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text

open class UltimateEnchantment(
    name: String,
    description: String,
    levels: IntRange,
    applicable: List<ItemType>,
    baseStats: Statistics = Statistics.zero(),
    baseSpecials: SpecialStatistics = SpecialStatistics(),
    multiplier: Float = 1f,
    conflicts: List<String> = listOf()
) : EnchantmentBase(
    name,
    description,
    levels,
    applicable,
    baseStats,
    baseSpecials,
    multiplier,
    conflicts
) {
    override fun displaySimple(level: Int): Component {
        return text("<light_purple><bold>$name ${roman(level)}</bold></light_purple>").noitalic()
    }
}
