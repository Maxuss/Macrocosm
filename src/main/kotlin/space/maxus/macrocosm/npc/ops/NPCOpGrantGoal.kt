package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture

/**
 * Grants player a goal
 */
data class NPCOpGrantGoal(
    val goal: String
): NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        data.player.reachGoal(goal)
        return CompletableFuture.completedFuture(Unit)
    }
}
