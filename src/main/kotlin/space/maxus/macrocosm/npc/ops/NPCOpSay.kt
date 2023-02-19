package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture

/**
 * Says something to player
 */
data class NPCOpSay(val message: String) : NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        data.player.sendMessage("<yellow>[NPC] ${data.self.name}<white>: $message")
        return CompletableFuture.completedFuture(Unit)
    }
}
