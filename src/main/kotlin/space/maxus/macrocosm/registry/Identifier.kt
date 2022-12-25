package space.maxus.macrocosm.registry

import net.minecraft.nbt.*
import net.minecraft.resources.ResourceLocation
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import java.io.DataOutput
import java.io.Serializable

data class Identifier(val namespace: String, val path: String) : Tag, Serializable {
    fun minecraft() = ResourceLocation(namespace, path)

    fun bukkit() = NamespacedKey(namespace, path)
    fun tag(): StringTag = StringTag.valueOf(toString())
    override fun toString() = "$namespace:$path"

    fun isNotNull() = this != NULL
    fun isNull() = this == NULL

    fun verify(): Boolean {
        return VALIDITY_REGEX.matches(namespace) && VALIDITY_REGEX.matches(path)
    }

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

    override fun sizeInBytes(): Int {
        return 36 + 2 * "$namespace:$path".length
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

    companion object : PersistentDataType<String, Identifier> {
        val NULL = macro("null")
        val VALIDITY_REGEX = Regex("[a-zA-Z_#$\\d]+")

        fun macro(path: String) = Identifier(Macrocosm.id, path)
        fun parse(value: String): Identifier {
            val split = value.split(':')
            if (split.size == 1)
                return macro(split[0])
            return Identifier(split[0], split[1])
        }

        fun fromMinecraft(rl: ResourceLocation) = Identifier(rl.namespace, rl.path)
        fun fromBukkit(nk: NamespacedKey) = Identifier(nk.namespace, nk.key)
        override fun getPrimitiveType(): Class<String> {
            return String::class.java
        }

        override fun getComplexType(): Class<Identifier> {
            return Identifier::class.java
        }

        override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): Identifier {
            return parse(primitive)
        }

        override fun toPrimitive(complex: Identifier, context: PersistentDataAdapterContext): String {
            return complex.toString()
        }
    }
}
