package space.maxus.macrocosm.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object CompoundDataType : PersistentDataType<ByteArray, CompoundTag> {
    override fun getPrimitiveType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getComplexType(): Class<CompoundTag> {
        return CompoundTag::class.java
    }

    override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): CompoundTag {
        return NbtIo.readCompressed(BufferedInputStream(ByteArrayInputStream(primitive)))
    }

    override fun toPrimitive(complex: CompoundTag, context: PersistentDataAdapterContext): ByteArray {
        val outputStream = ByteArrayOutputStream()
        NbtIo.writeCompressed(complex, outputStream)
        return outputStream.toByteArray()
    }
}
