package space.maxus.macrocosm.npc.ops

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

data class NPCOpWait(val seconds: Number): NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        val millis = seconds.toDouble() * 1000
        return CompletableFuture.supplyAsync({ }, CompletableFuture.delayedExecutor(millis.toLong(), TimeUnit.MILLISECONDS))
    }
}
