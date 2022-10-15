package space.maxus.macrocosm.async

import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.minecraft.server.MinecraftServer
import space.maxus.macrocosm.util.Fn
import space.maxus.macrocosm.util.Monitor
import space.maxus.macrocosm.util.threadNoinline
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
     * it is recommended to use [runAsync], which is more lightweight
     *
     * @param name Name of the new thread
     * @param isDaemon Whether the thread should be a daemon
     * @param runnable The task to be executed
     */
    inline fun contextBoundedRunAsync(
        name: String = "Worker Thread #${activeThreads.get()}",
        isDaemon: Boolean = false,
        crossinline runnable: ThreadContext.() -> Unit
    ) {
        activeThreads.incrementAndGet()
        val thr = thread(false, isDaemon = isDaemon, name = name) {
            Monitor.enter("runAsync $name")
            runnable(ThreadContext(name))
            activeThreads.decrementAndGet()
            Monitor.exit(Thread.currentThread())
        }
        Monitor.inject(thr)
        thr.start()
    }

    /**
     * Runs provided task on another thread **without** [ThreadContext], which
     * provides better performance.
     *
     * @param isDaemon Whether the thread is a daemon
     * @param runnable The code to be run
     */
    inline fun runAsync(
        isDaemon: Boolean = false,
        crossinline runnable: () -> Unit
    ) {
        threadNoinline(true, isDaemon = isDaemon, name = "Worker Thread #${activeThreads.incrementAndGet()}") {
            Monitor.inject(this)
            Monitor.enter("runAsyncRaw")
            runnable()
            activeThreads.decrementAndGet()
            Monitor.exit(this)
            interrupt()
        }
    }

    /**
     * Executes the code on separate thread if [Thread.currentThread] is
     * the current Minecraft Server thread ([MinecraftServer.serverThread]),
     * otherwise the code is run on current thread
     *
     * @param runnable The code to run
     */
    inline fun driftFromMain(
        crossinline runnable: () -> Unit
    ) {
        if (Thread.currentThread() == MinecraftServer.getServer().serverThread)
            runAsync(runnable = runnable)
        else
            runnable()
    }

    /**
     * Constructs a new cached thread pool, delegating to [Executors.newCachedThreadPool]
     *
     * @return New [ExecutorService] provided by cached thread pool
     */
    fun newCachedPool(): ExecutorService = Executors.newCachedThreadPool(
        ThreadFactoryBuilder().setUncaughtExceptionHandler(Monitor.exceptionHandler).build()
    )

    /**
     * Constructs a new fixed thread pool, with [max] amount of active threads at once
     *
     * @param max Max amount of threads
     * @return New [ExecutorService] provided by fixed thread pool
     */
    fun newFixedPool(max: Int): ExecutorService = Executors.newFixedThreadPool(
        max,
        ThreadFactoryBuilder().setUncaughtExceptionHandler(Monitor.exceptionHandler).build()
    )

    fun runEachConcurrently(service: ExecutorService = Executors.newCachedThreadPool(), vararg executors: Fn) {
        runAsync {
            Monitor.enter("runEachConcurrently")
            for (fn in executors) {
                service.execute(fn)
            }
            service.shutdown()
            Monitor.exit(Thread.currentThread())
        }
    }
}
