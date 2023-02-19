package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture

/**
 * Sends a raw message to player
 */
data class NPCOpSendMessage(val message: String) : NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        data.player.sendMessage(message)
        return CompletableFuture.completedFuture(Unit)
    }
}
