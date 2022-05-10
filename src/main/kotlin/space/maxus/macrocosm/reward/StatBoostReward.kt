package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

fun boostStat(stat: Statistic, base: Double, multiply: Boolean = false, hide: Boolean = false) = StatBoostReward(stat, base, multiply, hide)

class StatBoostReward(
    private val stat: Statistic, val base: Double, private val multiplicative: Boolean,
    override val isHidden: Boolean = false
): Reward {
    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        if(multiplicative) {
            player.baseStats[stat] /= (1 + (base * (lvl - 1)).toFloat())
            player.baseStats[stat] *= (1 + (base * lvl).toFloat())
        } else {
            player.baseStats[stat] = player.baseStats[stat] + base.toFloat()
        }
    }

    override fun display(lvl: Int): Component {
        return comp("<dark_gray>+<green>${Formatting.stats(base.toBigDecimal())} ${stat.display}").noitalic()
    }
}
