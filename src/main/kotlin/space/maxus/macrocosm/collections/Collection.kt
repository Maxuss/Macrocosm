package space.maxus.macrocosm.collections

import org.jetbrains.annotations.ApiStatus
import space.maxus.macrocosm.reward.Reward
import space.maxus.macrocosm.util.math.LevelingTable
import space.maxus.macrocosm.util.math.SkillTable
import java.io.Serializable

/**
 * Constructs a new instance of a collection
 */
@Deprecated("This is a redundant method", ReplaceWith("Collection(name, rewards, section, table)"))
@ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
fun collection(
    name: String,
    section: Section,
    rewards: List<Reward>,
    table: LevelingTable = SkillTable
) = Collection(name, rewards, section, table)

/**
 * A single collection which contains different items and rewards
 */
open class Collection(val name: String, val rewards: List<Reward>, val section: Section, val table: LevelingTable) :
    Serializable

