package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture

data class NPCOpSendMessage(val message: String): NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        data.player.sendMessage(message)
        return CompletableFuture.completedFuture(Unit)
    }
}
