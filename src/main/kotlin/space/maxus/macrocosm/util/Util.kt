package space.maxus.macrocosm.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.axay.kspigot.extensions.pluginKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.chat.ComponentTypeAdapter
import space.maxus.macrocosm.listeners.FallingBlockListener
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.IdentifierTypeAdapter
import space.maxus.macrocosm.stats.SpecialStatisticTypeAdapter
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.StatisticTypeAdapter
import space.maxus.macrocosm.stats.Statistics
import java.io.File
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists

val GSON: Gson = GsonBuilder().create()
val GSON_PRETTY: Gson = GsonBuilder()
    .disableHtmlEscaping()
    .registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
    .registerTypeAdapter(Statistics::class.java, StatisticTypeAdapter)
    .registerTypeAdapter(SpecialStatistics::class.java, SpecialStatisticTypeAdapter)
    .registerTypeAdapter(Component::class.java, ComponentTypeAdapter)
    .setPrettyPrinting().create()

fun String.stripTags() = MiniMessage.miniMessage().stripTags(this)

fun ticksToTime(ticks: Long): String {
    var hours = (ticks / 1000) + 6
    var minutes = (ticks % 1000) / (1000 / 60)
    while(minutes >= 60) {
        hours++
        minutes -= 60
    }
    val suffix = if(hours >= 24) {
        hours -= 24
        "AM"
    }
    else if(hours > 12) {
        hours -= 12
        "PM"
    } else "AM"
    return (if (hours < 10) "0" else "") + hours + ":" + (if (minutes < 10) "0" else "") + minutes + suffix
}

fun <K, V> ConcurrentHashMap<K, ConcurrentLinkedQueue<V>>.setOrAppend(key: K, value: V): Int {
    if(this.containsKey(key)) {
        val v = this[key]!!
        v.add(value)
        this[key] = v
        return v.indexOf(value)
    } else {
        this[key] = ConcurrentLinkedQueue<V>().apply { add(value) }
        return 0
    }
}

fun <L : PacketListener, P : Packet<L>> Player.sendPacket(packet: P) {
    (this as CraftPlayer).handle.networkManager.send(packet)
}

fun EntityType.summon(location: Location): Entity {
    val types = net.minecraft.world.entity.EntityType.byString(this.key.asString())
    val nmsEntity = (if (types.isPresent) {
        val entityTypes = types.get()
        entityTypes.create((location.world as CraftWorld).handle)
    } else null) ?: throw RuntimeException("Can not spawn an entity of type $this!")
    nmsEntity.bukkitEntity.teleport(location)
    return nmsEntity.bukkitEntity
}

fun createFloatingBlock(loc: Location, item: ItemStack): ArmorStand {
    val hologram: ArmorStand = loc.world.spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
    hologram.setGravity(false)
    hologram.canPickupItems = false
    hologram.isVisible = false
    hologram.isMarker = true
    hologram.persistentDataContainer.set(pluginKey("ignore_damage"), PersistentDataType.BYTE, 0)
    val fallingBlock = loc.world.spawnFallingBlock(loc, item.type.createBlockData())
    fallingBlock.dropItem = false
    FallingBlockListener.stands.add(fallingBlock.uniqueId)
    hologram.addPassenger(fallingBlock)
    return hologram
}

fun File.recreateFile() {
    if(this.exists())
        this.delete()
    this.createNewFile()
}

fun File.recreateDir() {
    if(this.exists())
        this.deleteRecursively()
    this.mkdirs()
}

fun Path.recreateFile() {
    this.deleteIfExists()
    this.createFile()
}

fun Path.recreateDir() {
    this.deleteIfExists()
    this.createDirectories()
}

fun threadScoped(
    start: Boolean = true,
    isDaemon: Boolean = false,
    contextClassLoader: ClassLoader? = null,
    name: String? = null,
    priority: Int = -1,
    block: Thread.() -> Unit
): Thread {
    val thread = object : Thread() {
        override fun run() {
            block()
        }
    }
    if (isDaemon)
        thread.isDaemon = true
    if (priority > 0)
        thread.priority = priority
    if (name != null)
        thread.name = name
    if (contextClassLoader != null)
        thread.contextClassLoader = contextClassLoader
    if (start)
        thread.start()
    return thread
}
