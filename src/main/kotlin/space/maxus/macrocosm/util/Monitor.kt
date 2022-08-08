package space.maxus.macrocosm.util

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimaps
import space.maxus.macrocosm.logger
import space.maxus.macrocosm.monitor
import java.lang.Thread.UncaughtExceptionHandler
import java.time.Instant

class Monitor {
    private val currentScopes = Multimaps.synchronizedMultimap<String, Scope>(HashMultimap.create())

    companion object {
        val exceptionHandler by lazy {
            UncaughtExceptionHandler { thr, err ->
                val lastEntered = monitor.monitorLast(thr)
                if (lastEntered != null)
                    logger.warning(
                        "Monitor: caught exception at ${lastEntered.name} in thread ${lastEntered.threadName}, previously entered at ${
                            Instant.ofEpochMilli(
                                lastEntered.timeStamp
                            )
                        }. Dumping exception data..."
                    )
                else
                    logger.warning("Monitor: caught exception at null scope! Dumping exception data...")
                logger.severe(err.stackTraceToString())
                monitor.exitAll(thr)
            }
        }

        fun inject(thread: Thread = Thread.currentThread()) {
            thread.uncaughtExceptionHandler = exceptionHandler
        }

        fun enter(scope: String) {
            monitor.enterSingle(scope)
        }

        fun exit(forThread: Thread = Thread.currentThread()) {
            monitor.exitSingle(forThread)
        }
    }

    fun enterSingle(scope: String) {
        synchronized(this.currentScopes) {
            val n = Thread.currentThread().name
            this.currentScopes.put(n, Scope(scope, n, Instant.now().toEpochMilli()))
        }
    }

    fun exitSingle(forThread: Thread) {
        synchronized(this.currentScopes) {
            val last = currentScopes[forThread.name].last()
            currentScopes.remove(forThread.name, last)
        }
    }

    fun exitAll(forThread: Thread) {
        synchronized(this.currentScopes) {
            currentScopes.removeAll(forThread.name)
        }
    }

    fun monitorLast(forThread: Thread): Scope? {
        synchronized(this.currentScopes) {
            return currentScopes[forThread.name].lastOrNull()
        }
    }

    data class Scope(val name: String, val threadName: String, val timeStamp: Long)
}
