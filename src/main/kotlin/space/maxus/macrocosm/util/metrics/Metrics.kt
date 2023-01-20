package space.maxus.macrocosm.util.metrics

import space.maxus.macrocosm.metrics.MacrocosmMetrics
import space.maxus.macrocosm.util.general.Callback

inline fun <R> report(message: String, callback: () -> R): R {
    return Callback {
        MacrocosmMetrics.logger.error(message)
        val msg = MacrocosmMetrics.counter("macrocosm_errors", "Macrocosm Errors").labels(message)
        msg.inc()
    }.then(callback)
}
