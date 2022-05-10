package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.comp

class CombinedReward(vararg rwd: Reward): Reward {
    private val rewards: MutableList<Reward> = mutableListOf(*rwd)
    override val isHidden: Boolean = false

    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        for(reward in rewards) {
            reward.reward(player, lvl)
        }
    }

    override fun display(lvl: Int): Component {
        var total = comp("")
        for(reward in rewards) {
            if(!reward.isHidden) {
                total = total.append(reward.display(lvl)).append(comp("\n"))
            }
        }
        return total
    }

    override infix fun and(other: Reward): CombinedReward {
        rewards.add(other)
        return this
    }
}
