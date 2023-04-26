package space.maxus.macrocosm.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram
import io.prometheus.client.Summary
import io.prometheus.client.exporter.HTTPServer
import okhttp3.internal.closeQuietly
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.maxus.macrocosm.async.Threading
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor

/**
 * Metrics class for Macrocosm.
 */
object MacrocosmMetrics {
    private lateinit var server: HTTPServer

    /**
     * Metrics logger
     */
    val logger: Logger = LoggerFactory.getLogger("MacrocosmMetrics")

    /**
     * Metrics thread executor
     */
    val metricThread: Executor = Threading.newFixedPool(16)

    private val gauges: ConcurrentHashMap<String, Gauge> = ConcurrentHashMap()
    private val counters: ConcurrentHashMap<String, Counter> = ConcurrentHashMap()
    private val histograms: ConcurrentHashMap<String, Histogram> = ConcurrentHashMap()
    private val summaries: ConcurrentHashMap<String, Summary> = ConcurrentHashMap()

    /**
     * Constructs or gets a gauge from cache with provided configuration
     */
    fun gauge(id: String, description: String? = null): Gauge {
        if (!gauges.containsKey(id))
            return Gauge.build(id, description ?: id).register().let {
                gauges[id] = it
                it
            }
        return gauges[id]!!
    }

    /**
     * Constructs or gets a counter from cache with provided configuration
     */
    fun counter(id: String, description: String? = null): Counter {
        if (!counters.containsKey(id))
            return Counter.build(id, description ?: id).register().let {
                counters[id] = it
                it
            }
        return counters[id]!!
    }

    /**
     * Constructs or gets a histogram from cache with provided configuration
     */
    fun histogram(id: String, description: String? = null): Histogram {
        if (!histograms.containsKey(id))
            return Histogram.build().name(id).help(description!!).register().let {
                histograms[id] = it
                it
            }
        return histograms[id]!!
    }

    /**
     * Constructs or gets a summary from cache with provided configuration
     */
    fun summary(id: String, description: String? = null, quantile: Double? = null, error: Double? = null): Summary {
        if (!summaries.containsKey(id))
            return Summary.build().name(id).help(description!!).quantile(quantile!!, error!!).register().let {
                summaries[id] = it
                it
            }
        return summaries[id]!!
    }

    /**
     * Initializes Metrics on port 3438
     */
    fun init() {
        server = HTTPServer(3438)
    }

    /**
     * Shuts down Metrics server
     */
    fun shutdown() {
        server.closeQuietly()
    }
}
