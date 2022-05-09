package space.maxus.macrocosm.async

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Threading {
    val pool: ExecutorService = Executors.newCachedThreadPool()

    fun start(runnable: () -> Unit) {
        pool.execute(runnable)
    }

    fun pool(): ExecutorService = Executors.newCachedThreadPool()
}
