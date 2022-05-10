package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.text.comp

class SkillExperienceReward(val skill: SkillType, val exp: Double, override val isHidden: Boolean = false): Reward {
    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        player.addSkillExperience(skill, lvl * exp)
    }

    override fun display(lvl: Int): Component {
        return comp("<dark_gray>+<dark_aqua>${Formatting.withCommas(exp.toBigDecimal(), true)} ${skill.inst.name} EXP").noitalic()
    }
}
