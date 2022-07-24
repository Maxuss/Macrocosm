package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.text

interface Reward {
    val isHidden: Boolean
    fun reward(player: MacrocosmPlayer, lvl: Int)
    fun display(lvl: Int): Component

    infix fun and(other: Reward) = CombinedReward(this, other)

    fun repeating(size: Int = 50): List<Reward> {
        return List(size) { this }
    }

    companion object : Reward {
        override val isHidden: Boolean = false

        override fun reward(player: MacrocosmPlayer, lvl: Int) {
            player.sendMessage("<gray>You have just received a placeholder reward! <italic><dark_gray>Something is wrong...")
        }

        override fun display(lvl: Int): Component {
            return text("<gray>Placeholder reward $lvl!").noitalic()
        }

    }
}
