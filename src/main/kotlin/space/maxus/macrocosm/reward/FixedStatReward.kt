package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text

class FixedStatReward(val stat: Statistic, val amount: Float, override val isHidden: Boolean = false) : Reward {
    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        player.baseStats[stat] += amount
    }

    override fun display(lvl: Int): Component {
        return text("<${stat.color.asHexString()}>Permanent +${Formatting.stats(amount.toBigDecimal())} ${stat.display}")
    }
}
