package space.maxus.macrocosm.serde

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.math.BigDecimal
import java.util.*

data class Serialization(private val stream: DataOutputStream, private val byteStream: ByteArrayOutputStream) {
    fun end(): String {
        stream.flush()
        val bytes = byteStream.toByteArray()
        stream.close()
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun int(int: Int): Serialization {
        stream.writeInt(int)
        return this
    }

    fun int(int: Long): Serialization {
        stream.writeLong(int)
        return this
    }

    fun decimal(dec: BigDecimal): Serialization {
        stream.write(dec.toBigInteger().toByteArray())
        return this
    }

    fun string(str: String): Serialization {
        stream.writeBytes(str)
        return this
    }
}
