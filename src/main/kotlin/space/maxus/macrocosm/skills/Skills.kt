package space.maxus.macrocosm.skills

import space.maxus.macrocosm.serde.Bytes
import space.maxus.macrocosm.util.general.defer
import java.io.Serializable

class Skills(val skillExp: HashMap<SkillType, PlayerSkill>) : Serializable {

    operator fun get(sk: SkillType): Double {
        return skillExp[sk]!!.overflow
    }

    operator fun set(sk: SkillType, exp: Double) {
        skillExp[sk]!!.overflow = exp
    }

    fun increase(sk: SkillType, exp: Double): Boolean {
        val skill = skillExp[sk]!!
        skill.overflow += exp
        return sk.inst.table.shouldLevelUp(skill.lvl, skill.overflow, .0)
    }

    fun level(sk: SkillType): Int {
        return skillExp[sk]!!.lvl
    }

    fun setLevel(sk: SkillType, lvl: Int) {
        skillExp[sk]!!.overflow = .0
        skillExp[sk]!!.lvl = lvl
    }

    fun serialize(): String {
        return Bytes.serialize().obj(this).end()
    }

    companion object {
        fun default(): Skills = Skills(HashMap(SkillType.values().associateWith { PlayerSkill(1, .0) }))

        fun deserialize(data: String): Skills {
            return Bytes.deserialize(data).defer { end() }.first { obj() }
        }
    }
}

data class PlayerSkill(var lvl: Int, var overflow: Double) : Serializable
