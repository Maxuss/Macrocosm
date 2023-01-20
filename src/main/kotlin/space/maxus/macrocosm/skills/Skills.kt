package space.maxus.macrocosm.skills

import io.prometheus.client.Counter
import space.maxus.macrocosm.metrics.MacrocosmMetrics
import space.maxus.macrocosm.serde.Bytes
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
        MacrocosmMetrics.metricThread.execute {
            val skillMetrics = metrics(sk)
            if(skillMetrics.get() < skill.overflow)
                skillMetrics.inc(skill.overflow - skillMetrics.get())
            else
                metrics(sk).inc(exp)
            totalSkillExperience.inc(exp)
        }
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
        val totalSkillExperience by lazy { MacrocosmMetrics.counter("skills_total_exp", "Total Skill Experience") }
        private fun metrics(skill: SkillType): Counter {
            return MacrocosmMetrics.counter("skills_${skill.name.lowercase()}_total_exp", "Total experience for certain skill")
        }

        fun default(): Skills = Skills(HashMap(SkillType.values().associateWith { PlayerSkill(1, .0) }))
    }
}

data class PlayerSkill(var lvl: Int, var overflow: Double) : Serializable
