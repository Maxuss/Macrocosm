package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text

/**
 * An ultimate enchantment. In implementation details differs from [SimpleEnchantment] only by having light purple bold name.
 * All the other logic is hard coded inside the [MacrocosmItem][space.maxus.macrocosm.item.MacrocosmItem]
 *
 * @param name name of the enchantment
 * @param description description of the enchantment
 * @param levels possible levels of the enchantment
 * @param applicable item types this enchantment is applicable to
 * @param baseStats base statistics that this enchantment applies, excluding the level modifier
 * @param baseSpecials base special statistics that this enchantment applies, excluding the level modifier
 * @param multiplier the multiplier that is applied each level
 * @param conflicts IDs of enchantments this enchantment conflicts with
 */
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
