package space.maxus.macrocosm.skills

import com.google.gson.reflect.TypeToken
import space.maxus.macrocosm.util.GSON

class Skills(val skillExp: HashMap<SkillType, PlayerSkill>) {

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

    fun json(): String {
        return GSON.toJson(skillExp.map { (key, value) -> Pair(key.name, value) }.toMap())
    }

    companion object {
        fun default(): Skills = Skills(HashMap(SkillType.values().associateWith { PlayerSkill(1, .0) }))

        fun fromJson(json: String): Skills {
            val map: HashMap<String, PlayerSkill> =
                GSON.fromJson(json, object : TypeToken<HashMap<String, PlayerSkill>>() {}.type)
            return Skills(HashMap(map.map { (key, value) -> Pair(SkillType.valueOf(key), value) }.toMap()))
        }
    }
}

data class PlayerSkill(var lvl: Int, var overflow: Double)
