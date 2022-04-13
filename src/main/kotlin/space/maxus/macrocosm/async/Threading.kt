package space.maxus.macrocosm.async

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Threading {
    val pool: ExecutorService = Executors.newFixedThreadPool(10)

    fun start(runnable: () -> Unit) {
        pool.execute(runnable)
    }
}
