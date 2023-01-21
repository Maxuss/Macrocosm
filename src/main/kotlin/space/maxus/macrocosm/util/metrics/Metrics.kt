package space.maxus.macrocosm.util.metrics

import io.prometheus.client.Counter
import org.jetbrains.annotations.ApiStatus.Internal
import space.maxus.macrocosm.metrics.MacrocosmMetrics
import space.maxus.macrocosm.util.general.Callback

@Internal
val counterErrors: Counter =
    Counter.build().name("macrocosm_errors").help("Macrocosm Errors").labelNames("Error").register()

inline fun <R> report(message: String, callback: () -> R): R {
    return Callback {
        MacrocosmMetrics.logger.error(message)
        val msg = counterErrors.labels(message)
        msg.inc()
    }.then(callback)
}
