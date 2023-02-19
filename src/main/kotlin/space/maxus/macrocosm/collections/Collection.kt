package space.maxus.macrocosm.collections

import space.maxus.macrocosm.collections.table.CollectionTable
import space.maxus.macrocosm.reward.Reward
import java.io.Serializable

/**
 * A single collection which contains different items and rewards
 */
open class Collection(
    val name: String,
    val rewards: List<Reward>,
    val section: CollectionSection,
    val table: CollectionTable
) :
    Serializable

