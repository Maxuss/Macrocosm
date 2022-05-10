package space.maxus.macrocosm.util

import net.minecraft.nbt.*
import net.minecraft.resources.ResourceLocation
import org.bukkit.NamespacedKey
import space.maxus.macrocosm.Macrocosm
import java.io.DataOutput

fun CompoundTag.getId(key: String) = Identifier.parse(getString(key))
fun CompoundTag.putId(key: String, id: Identifier) = put(key, id)
val ResourceLocation.macrocosm get() = Identifier.fromMinecraft(this)
val NamespacedKey.macrocosm get() = Identifier.fromBukkit(this)

fun id(namespace: String, path: String) = Identifier(namespace, path)
fun id(path: String) = Identifier.macro(path)

data class Identifier(val namespace: String, val path: String) : Tag {
    fun minecraft() = ResourceLocation(namespace, path)

    @Suppress("DEPRECATION")
    fun bukkit() = NamespacedKey(namespace, path)
    fun tag(): StringTag = StringTag.valueOf(toString())
    override fun toString() = "$namespace:$path"

    fun isNotNull() = this != NULL
    fun isNull() = this == NULL

    override fun write(output: DataOutput) {
        StringTag.valueOf(toString()).write(output)
    }

    override fun getId(): Byte {
        return Tag.TAG_STRING
    }

    override fun getType(): TagType<*> {
        return StringTag.TYPE
    }

    override fun copy(): Tag {
        return tag()
    }

    override fun accept(visitor: TagVisitor) {
        return tag().accept(visitor)
    }

    override fun accept(visitor: StreamTagVisitor): StreamTagVisitor.ValueResult {
        return tag().accept(visitor)
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is Identifier && other.namespace == this.namespace && other.path == this.path
    }

    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    companion object {
        val NULL = macro("null")

        fun macro(path: String) = Identifier(Macrocosm.id, path)
        fun parse(value: String): Identifier {
            val split = value.split(':')
            return Identifier(split[0], split[1])
        }

        fun fromMinecraft(rl: ResourceLocation) = Identifier(rl.namespace, rl.path)
        fun fromBukkit(nk: NamespacedKey) = Identifier(nk.namespace, nk.key)
    }
}
