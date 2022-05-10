package space.maxus.macrocosm.skills

import com.google.gson.reflect.TypeToken
import space.maxus.macrocosm.util.GSON

class Skills(private val skillExp: HashMap<SkillType, PlayerSkill>) {

    operator fun get(sk: SkillType): Double {
        return skillExp[sk]!!.total
    }

    operator fun set(sk: SkillType, exp: Double) {
        skillExp[sk]!!.total = exp
    }

    fun increase(sk: SkillType, exp: Double): Boolean {
        skillExp[sk]!!.total = skillExp[sk]!!.total + exp
        return sk.inst.table.shouldLevelUp(skillExp[sk]!!.lvl, skillExp[sk]!!.total, exp)
    }

    fun level(sk: SkillType): Int {
        return skillExp[sk]!!.lvl
    }

    fun setLevel(sk: SkillType, lvl: Int) {
        skillExp[sk]!!.lvl = lvl
    }

    fun json(): String {
        return GSON.toJson(skillExp.map { (key, value) -> Pair(key.name, value) }.toMap())
    }

    companion object {
        fun default(): Skills = Skills(HashMap(SkillType.values().associateWith { PlayerSkill(0, .0) }))

        fun fromJson(json: String): Skills {
            val map: HashMap<String, PlayerSkill> =
                GSON.fromJson(json, object : TypeToken<HashMap<String, PlayerSkill>>() {}.type)
            return Skills(HashMap(map.map { (key, value) -> Pair(SkillType.valueOf(key), value) }.toMap()))
        }
    }
}

data class PlayerSkill(var lvl: Int, var total: Double)
