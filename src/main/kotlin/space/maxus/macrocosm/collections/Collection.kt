package space.maxus.macrocosm.collections

import space.maxus.macrocosm.reward.Reward
import space.maxus.macrocosm.util.math.LevelingTable
import java.io.Serializable

/**
 * A single collection which contains different items and rewards
 */
open class Collection(val name: String, val rewards: List<Reward>, val section: Section, val table: LevelingTable) :
    Serializable

