package space.maxus.macrocosm.serde

import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

object Bytes {
    fun serialize(): Serialization {
        val bytes = ByteArrayOutputStream()
        val out = DataOutputStream(BufferedOutputStream(bytes))
        return Serialization(out, bytes)
    }
}
