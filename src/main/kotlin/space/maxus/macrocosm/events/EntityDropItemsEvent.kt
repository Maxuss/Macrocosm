package space.maxus.macrocosm.events

import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.entity.loot.LootPool

class EntityDropItemsEvent(
    val killer: Entity?,
    val entity: LivingEntity,
    var pool: LootPool
) : Event(), Cancellable {
    private var eventCancelled = false

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }

    }

    override fun getHandlers() = HANDLERS
    override fun isCancelled() = eventCancelled

    override fun setCancelled(cancel: Boolean) {
        eventCancelled = cancel
    }
}
