package space.maxus.macrocosm.serde

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

object Bytes {
    fun serialize(): Serialization {
        val bytes = ByteArrayOutputStream()
        val out = DataOutputStream(BufferedOutputStream(bytes))
        return Serialization(out, bytes)
    }

    fun deserialize(base64: String): Deserialization {
        val stream = DataInputStream(BufferedInputStream(ByteArrayInputStream(Base64.getDecoder().decode(base64))))
        return Deserialization(stream)
    }
}
