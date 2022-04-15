package space.maxus.macrocosm.reforge

import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistics

@Suppress("unused")
interface Reforge {
    val ability: String?
    val name: String

    fun stats(rarity: Rarity): Statistics
}
