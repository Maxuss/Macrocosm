package space.maxus.macrocosm.skills

import space.maxus.macrocosm.reward.Reward
import space.maxus.macrocosm.util.math.LevelingTable
import space.maxus.macrocosm.util.math.SkillTable

abstract class Skill(val name: String, val rewards: List<Reward>, val table: LevelingTable)

class SimpleSkill(name: String, rewards: List<Reward>, table: LevelingTable = SkillTable) : Skill(name, rewards, table)

fun skill(name: String, rewards: List<Reward>, table: LevelingTable = SkillTable) = SimpleSkill(name, rewards, table)
