package space.maxus.macrocosm.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import space.maxus.macrocosm.entity.loot.LootPool
import space.maxus.macrocosm.entity.loot.vanilla
import space.maxus.macrocosm.events.BlockDropItemsEvent
import space.maxus.macrocosm.players.macrocosm

object BlockBreakListener: Listener {
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val mc = e.player.macrocosm ?: return
        val pool = LootPool.of(*e.block.getDrops(e.player.itemInUse).map { vanilla(it.type, 1.0, amount = it.amount..it.amount) }.toTypedArray())
        val event = BlockDropItemsEvent(mc, e.block, pool)
        event.callEvent()
        val items = event.pool.roll(mc)

        for (item in items) {
            e.block.world.dropItemNaturally(e.block.location, item ?: continue)
        }
        e.isDropItems = false
    }
}
