package space.maxus.macrocosm.reforge

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistics
import kotlin.math.max

class SimpleReforge(
    override val name: String,
    override val applicable: List<ItemType>,
    private val baseStats: Statistics,
    private val multiplier: Float = 1f,
    override val abilityName: String? = null,
    override val abilityDescription: String? = null
) : Reforge {

    override fun stats(rarity: Rarity): Statistics {
        val clone = baseStats.clone()
        clone.multiply(1 + (multiplier * max(rarity.ordinal, 1)))
        return clone
    }

    override fun clone(): Reforge {
        return SimpleReforge(name, applicable, baseStats, multiplier)
    }
}
