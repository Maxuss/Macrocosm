package space.maxus.macrocosm.events

import org.bukkit.entity.LivingEntity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.players.MacrocosmPlayer

class PlayerDealDamageEvent(
    val player: MacrocosmPlayer,
    val damaged: LivingEntity,
    var damage: Float,
    var crit: Boolean,
    val kind: DamageKind,
    var isSuperCrit: Boolean = false
) : Event(), Cancellable {
    val isContact get() = kind == DamageKind.MELEE
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
