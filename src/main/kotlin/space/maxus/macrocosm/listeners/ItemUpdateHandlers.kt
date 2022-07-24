package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.pluginKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object ItemUpdateHandlers : Listener {
    @EventHandler
    fun onItemSwap(e: PlayerItemHeldEvent) {
        val mc = e.player.macrocosm ?: return
        val player = e.player

        val item = player.inventory.getItem(e.newSlot) ?: return
        val pdc = item.itemMeta.persistentDataContainer
        val key = pluginKey("LAST_ITEM_UPDATE")
        val time = System.currentTimeMillis()
        // only update items every 5 seconds
        if (pdc.has(key) && (time - pdc.get(
                key,
                PersistentDataType.LONG
            )!!) >= 5L.toDuration(DurationUnit.SECONDS).inWholeMilliseconds
        ) {
            val i = item.macrocosm!!.build(mc)!!
            i.itemMeta.persistentDataContainer.set(key, PersistentDataType.LONG, time)
            player.inventory.setItem(e.newSlot, i)
        } else if (!pdc.has(key)) {
            val i = item.macrocosm!!.build(mc)!!
            i.itemMeta.persistentDataContainer.set(key, PersistentDataType.LONG, time)
            player.inventory.setItem(e.newSlot, i)
        }
    }
}
