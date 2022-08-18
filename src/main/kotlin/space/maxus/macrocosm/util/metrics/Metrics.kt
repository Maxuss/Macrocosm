package space.maxus.macrocosm.util.metrics

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.toVarIntBytes
import space.maxus.macrocosm.util.general.Callback
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.ExecutorService

interface Metrics {
    fun send(type: MetricType, msg: ByteArray): Boolean

    companion object {
        var handler: Metrics? = null
        val logger: Logger = LoggerFactory.getLogger("MacrocosmMetrics")

        val metricThread: ExecutorService = Threading.newFixedPool(6)
        const val VERSION: Int = 1_0_0

        fun send(type: MetricType, msg: ByteArray) {
            handler?.send(type, msg)
        }
    }
}

inline fun <R> report(message: String, callback: () -> R): R {
    return Callback {
        Metrics.logger.error(message)
        if (Metrics.handler == null)
            return@Callback
        Metrics.metricThread.execute {
            Metrics.send(MetricType.REPORT, constructReportBytes(message, MetricType.REPORT))
        }
    }.then(callback)
}

fun constructReportBytes(message: String, type: MetricType): ByteArray {
    val baos = ByteArrayOutputStream()
    val buffered = BufferedOutputStream(baos)

    buffered.write(type.ordinal) // index
    writeBasicHeader(buffered)
    str(message, buffered)

    buffered.flush()
    return baos.toByteArray()
}

private fun writeBasicHeader(output: OutputStream) {
    str(Macrocosm.description.name, output)
    str(Macrocosm.description.version, output)
    output.write(Metrics.VERSION.toVarIntBytes())
}

private fun str(string: String, os: OutputStream) {
    val slice = string.encodeToByteArray()
    val size = slice.size.toVarIntBytes()
    os.write(size)
    os.write(slice)
}
