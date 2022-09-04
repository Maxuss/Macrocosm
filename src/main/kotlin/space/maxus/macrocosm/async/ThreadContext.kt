package space.maxus.macrocosm.async

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Context for runnables created using [Threading.contextBoundedRunAsync]
 *
 * @param threadName Name of the thread used for logger
 */
class ThreadContext(threadName: String) {
    private val log: Logger = LoggerFactory.getLogger(threadName)

    fun info(msg: String) = log.info(msg)
}
