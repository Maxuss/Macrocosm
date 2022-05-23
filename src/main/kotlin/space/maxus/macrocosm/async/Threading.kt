package space.maxus.macrocosm.async

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * An object useful for running multithreaded asynchronous code
 */
object Threading {
    /**
     * Amount of threads active at a time
     */
    val activeThreads: AtomicInteger = AtomicInteger(1)

    /**
     * Runs provided task on another thread, with ThreadContext.
     * This should not be used if called lots of times, e.g. in a loop, because
     * creating of numerous [ThreadContext]s might cause a memory leak, in that case
     * it is recommended to use [runAsyncRaw], which is more lightweight
     *
     * @param name Name of the new thread
     * @param isDaemon Whether the thread should be a daemon
     * @param runnable The task to be executed
     */
    inline fun runAsync(
        name: String = "Worker Thread #${activeThreads.get()}",
        isDaemon: Boolean = false,
        crossinline runnable: ThreadContext.() -> Unit
    ) {
        activeThreads.incrementAndGet()
        thread(true, isDaemon = isDaemon, name = name) {
            runnable(ThreadContext(name))
            activeThreads.decrementAndGet()
        }
    }

    /**
     * Runs provided task on another thread **without** [ThreadContext], which
     * provides better performance.
     *
     * @param isDaemon Whether the thread is a daemon
     * @param runnable The code to be ran
     */
    inline fun runAsyncRaw(
        isDaemon: Boolean = false,
        crossinline runnable: () -> Unit
    ) {
        thread(true, isDaemon = isDaemon, name = "Worker Thread #${activeThreads.incrementAndGet()}") {
            runnable()
            activeThreads.decrementAndGet()
        }
    }

    /**
     * Constructs a new cached thread pool, delegating to [Executors.newCachedThreadPool]
     *
     * @return New [ExecutorService] provided by cached thread pool
     */
    fun newCachedPool(): ExecutorService = Executors.newCachedThreadPool()
}
