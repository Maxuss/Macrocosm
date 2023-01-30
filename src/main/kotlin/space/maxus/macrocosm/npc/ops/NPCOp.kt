package space.maxus.macrocosm.npc.ops

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import space.maxus.macrocosm.npc.MacrocosmNPC
import space.maxus.macrocosm.players.MacrocosmPlayer
import java.util.concurrent.CompletableFuture

/**
 * A global interface for all NPC operations
 */
interface NPCOp {
    /**
     * Processes this operation
     */
    fun operate(data: NPCOperationData): CompletableFuture<Unit>
}

/**
 * Context for an NPC dialogue operation
 */
data class NPCOperationData(val player: MacrocosmPlayer, val playerPaper: Player, val self: MacrocosmNPC, val selfPaper: Entity)
