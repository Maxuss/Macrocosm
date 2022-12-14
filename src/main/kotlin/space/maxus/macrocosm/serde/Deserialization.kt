package space.maxus.macrocosm.serde

import java.io.ObjectInputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer

@Suppress("UNCHECKED_CAST")
class Deserialization(private val input: ObjectInputStream) {
    fun end() {
        this.input.close()
    }

    fun int(): Int {
        return input.readInt()
    }

    fun long(): Long {
        return input.readLong()
    }

    fun decimal(): BigDecimal {
        val size = input.readInt()
        val buffer = input.readNBytes(size)
        return BigInteger(buffer).toBigDecimal()
    }

    fun string(): String {
        val buffer = mutableListOf<Byte>()
        var currentByte: Byte
        while (input.readByte().let { currentByte = it; currentByte } != 0x00.toByte()) {
            buffer.add(currentByte)
        }
        return Charsets.UTF_8.decode(ByteBuffer.wrap(buffer.toByteArray())).toString()
    }

    fun <T> obj(): T {
        return input.readObject() as T
    }
}
