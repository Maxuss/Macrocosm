package space.maxus.macrocosm.async

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

object Threading {
    val activeThreads: AtomicInteger = AtomicInteger(1)

    inline fun start(
        name: String = "Worker Thread / $activeThreads",
        isDaemon: Boolean = false,
        crossinline runnable: ThreadContext.() -> Unit
    ) {
        activeThreads.incrementAndGet()
        thread(true, isDaemon = isDaemon, name = name) {
            runnable(ThreadContext(name))
            activeThreads.decrementAndGet()
        }
    }

    fun pool(): ExecutorService = Executors.newCachedThreadPool()
}
