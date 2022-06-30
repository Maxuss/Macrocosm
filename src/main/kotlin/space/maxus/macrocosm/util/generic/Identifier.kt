package space.maxus.macrocosm.util.generic

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import org.bukkit.NamespacedKey
import space.maxus.macrocosm.registry.Identifier

fun CompoundTag.getId(key: String) = Identifier.parse(getString(key))
fun CompoundTag.putId(key: String, id: Identifier) = put(key, id)
val ResourceLocation.macrocosm get() = Identifier.fromMinecraft(this)
val NamespacedKey.macrocosm get() = Identifier.fromBukkit(this)

fun id(namespace: String, path: String) = Identifier(namespace, path)
fun id(path: String) = Identifier.macro(path)

