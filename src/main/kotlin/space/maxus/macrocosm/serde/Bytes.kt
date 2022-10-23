package space.maxus.macrocosm.serde

import space.maxus.macrocosm.util.general.defer
import java.io.*
import java.util.*

object Bytes {
    fun serialize(): Serialization {
        val bytes = ByteArrayOutputStream()
        val out = ObjectOutputStream(BufferedOutputStream(bytes))
        return Serialization(out, bytes)
    }

    fun deserialize(base64: String): Deserialization {
        val stream = ObjectInputStream(BufferedInputStream(ByteArrayInputStream(Base64.getDecoder().decode(base64))))
        return Deserialization(stream)
    }

    fun <T> deserializeObject(base64: String): T {
        return deserialize(base64).defer { end() }.first { obj() }
    }
}
