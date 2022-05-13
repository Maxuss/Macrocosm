package space.maxus.macrocosm.async

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ThreadContext(threadName: String) {
    private val log: Logger = LoggerFactory.getLogger(threadName)

    fun info(msg: String) = log.info(msg)
}
