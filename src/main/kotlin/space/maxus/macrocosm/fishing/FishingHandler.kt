package space.maxus.macrocosm.fishing

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import space.maxus.macrocosm.players.macrocosm

object FishingHandler: Listener {
    @EventHandler
    fun onPullHook(e: PlayerFishEvent) {
        if(e.state != PlayerFishEvent.State.CAUGHT_FISH)
            return
        FishingRegistry.possibleRewards(e.player.location, e.player.macrocosm!!).roll(e.player.macrocosm!!, e.hook)
    }

    @EventHandler
    fun onHookInLava(e: EntityDeathEvent) {
        if(e.entityType != EntityType.FISHING_HOOK)
            return
        e.isCancelled = true
    }
}
