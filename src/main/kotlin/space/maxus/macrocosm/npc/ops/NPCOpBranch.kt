package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture

data class NPCOpBranch(
    val condition: (NPCOperationData) -> Boolean,
    val trueBranch: List<NPCOp>,
    val falseBranch: List<NPCOp>
): NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        if(condition(data)) {
            for(success in trueBranch) {
                success.operate(data).get()
            }
        } else {
            for(failure in falseBranch) {
                failure.operate(data).get()
            }
        }
        return CompletableFuture.completedFuture(Unit)
    }
}
