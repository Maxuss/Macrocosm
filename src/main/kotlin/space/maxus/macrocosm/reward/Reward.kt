package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.players.MacrocosmPlayer

interface Reward {
    val isHidden: Boolean
    fun reward(player: MacrocosmPlayer, lvl: Int)
    fun display(lvl: Int): Component

    infix fun and(other: Reward) = CombinedReward(this, other)

    fun repeating(size: Int = 50): List<Reward> {
        return List(size) { this }
    }
}
