package space.maxus.macrocosm.collections

import space.maxus.macrocosm.reward.Reward
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.SkillTable

fun collection(
    name: String,
    section: Section,
    rewards: List<Reward>,
    table: LevelingTable = SkillTable
) = object: Collection(name, rewards, section, table) { }

abstract class Collection(val name: String, val rewards: List<Reward>, val section: Section, val table: LevelingTable)

