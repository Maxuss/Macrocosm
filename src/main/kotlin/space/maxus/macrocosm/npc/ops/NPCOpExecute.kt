package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture

/**
 * Executes provided code
 */
data class NPCOpExecute(val branch: (NPCOperationData) -> Unit) : NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync { branch(data) }
    }
}
