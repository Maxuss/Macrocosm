package space.maxus.macrocosm.util.data

import java.io.BufferedInputStream
import java.io.BufferedOutputStream

interface ByteConvert {
    fun toBytes(buffer: BufferedOutputStream)
}

interface ByteConvertOwned<out T: ByteConvertOwned<T>> {
    fun fromBytes(buffer: BufferedInputStream): T
}
