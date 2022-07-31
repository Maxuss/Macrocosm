package space.maxus.macrocosm.listeners

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityChangeBlockEvent
import java.util.*

object FallingBlockListener : Listener {
    val stands: MutableList<UUID> = mutableListOf()

    @EventHandler
    fun onBlockLand(e: EntityChangeBlockEvent) {
        if (e.entity is FallingBlock && stands.contains(e.entity.uniqueId)) {
            stands.remove(e.entity.uniqueId)
            e.isCancelled = true
        }
    }

    fun spawnBlock(at: Location, bd: BlockData): FallingBlock {
        val fallingBlock = at.world.spawnFallingBlock(at, bd)
        fallingBlock.setHurtEntities(false)
        fallingBlock.dropItem = false
        stands.add(fallingBlock.uniqueId)
        return fallingBlock
    }
}
