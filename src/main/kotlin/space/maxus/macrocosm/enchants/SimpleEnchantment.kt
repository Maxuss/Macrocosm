package space.maxus.macrocosm.enchants

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

/**
 * A simple enchantment to avoid unneeded inheritance
 *
 * @param name name of the enchantment
 * @param description description of the enchantment
 * @param levels possible levels of the enchantment
 * @param applicable item types this enchantment is applicable to
 * @param base base statistics that this enchantment applies, excluding the level modifier
 * @param special base special statistics that this enchantment applies, excluding the level modifier
 * @param multiplier the multiplier that is applied each level
 * @param conflicts IDs of enchantments this enchantment conflicts with
 */
class SimpleEnchantment(
    name: String,
    description: String,
    levels: IntRange,
    applicable: List<ItemType>,
    base: Statistics = Statistics.zero(),
    special: SpecialStatistics = SpecialStatistics(),
    multiplier: Float = 1f,
    conflicts: List<String> = listOf()
) : EnchantmentBase(
    name,
    description,
    levels,
    applicable,
    base,
    special,
    multiplier,
    conflicts
)
