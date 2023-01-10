package space.maxus.macrocosm.skills

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.reward.Reward

object AlchemyReward : Reward, Listener {
    override val isHidden: Boolean = true

    @EventHandler
    fun reward(e: PlayerPickupExperienceEvent) {
        val lvl = e.player.macrocosm?.skills?.level(SkillType.ALCHEMY) ?: return
        val boost = 1 + (lvl * 4)
        e.experienceOrb.experience *= boost
    }

    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        // unused
    }

    override fun display(lvl: Int): Component {
        return Component.empty()
    }
}
