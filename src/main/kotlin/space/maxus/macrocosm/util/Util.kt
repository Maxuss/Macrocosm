package space.maxus.macrocosm.util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.ComponentTypeAdapter
import space.maxus.macrocosm.exceptions.macrocosm
import space.maxus.macrocosm.listeners.FallingBlockListener
import space.maxus.macrocosm.pack.PackProvider
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.IdentifierTypeAdapter
import space.maxus.macrocosm.stats.SpecialStatisticTypeAdapter
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.StatisticTypeAdapter
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.metrics.report
import java.io.File
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.deleteIfExists
import kotlin.math.*
import kotlin.random.Random

val GSON: Gson = GsonBuilder()
    .registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
    .registerTypeAdapter(Statistics::class.java, StatisticTypeAdapter)
    .registerTypeAdapter(SpecialStatistics::class.java, SpecialStatisticTypeAdapter)
    .registerTypeAdapter(Component::class.java, ComponentTypeAdapter)
    .create()
val GSON_PRETTY: Gson = GsonBuilder()
    .disableHtmlEscaping()
    .registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
    .registerTypeAdapter(Statistics::class.java, StatisticTypeAdapter)
    .registerTypeAdapter(SpecialStatistics::class.java, SpecialStatisticTypeAdapter)
    .registerTypeAdapter(Component::class.java, ComponentTypeAdapter)
    .setPrettyPrinting().create()

typealias NULL = Unit
typealias Fn = () -> Unit
typealias FnArg<A> = (A) -> Unit
typealias FnRet<B> = () -> B
typealias FnArgRet<A, B> = (A) -> B

inline fun <reified T : Enum<T>> ClosedRange<T>.toList(values: FnRet<Array<out T>>): List<T> {
    val intRange = this.start.ordinal..this.endInclusive.ordinal
    return values().toList().slice(intRange)
}

operator fun <E> List<E>.get(intRange: IntRange): List<E> {
    return slice(intRange)
}

inline fun <reified T> Collection<T>.insertWithin(dummy: T, demand: Int): Collection<T> {
    if (size >= demand || demand <= 0)
        return this

    val list = mutableListOf<T>()
    val breaks = demand - size
    val breakSize = (breaks / max((size - 2), 2))
    val lastIndex = size - 1
    val repeated = dummy.repeated(breakSize)
    // demand = 14
    // size = 2
    // breaks = 12
    // breakSize = (12 / 2) = 6
    forEachIndexed { i, e ->
        list.add(e)
        if (i != lastIndex)
            list.addAll(repeated)
    }
    return list
}

val Inventory.emptySlots: Int
    get() {
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        return this.storageContents!!.count { stack -> stack.isAirOrNull() }
    }

fun <V, C : Iterable<V>> Iterable<C>.unwrapInner(): List<V> {
    val out = mutableListOf<V>()
    for (part in this) {
        out.addAll(part)
    }
    return out
}

fun Iterable<Double>.median(): Double {
    val sorted = this.sorted()
    if (sorted.isEmpty())
        return .0
    return if (sorted.size % 2 == 0) {
        ((sorted[sorted.size / 2] + sorted[sorted.size / 2 - 1]) / 2f)
    } else {
        (sorted[sorted.size / 2])
    }
}

fun <A> identity(): FnArgRet<A, A> = { a -> a }
fun nullFn(): FnRet<Unit> = { }

inline fun <reified V> aggregate(times: Int, crossinline generator: FnRet<V>): List<V> {
    return List(times, ignoringProducer(generator))
}

fun <V> Iterable<V>.withAll(other: Iterable<V>): List<V> {
    val new = mutableListOf<V>()
    new.addAll(this)
    new.addAll(other)
    return new
}

fun <K, V> ConcurrentHashMap<K, V>.withAll(other: ConcurrentHashMap<K, V>): ConcurrentHashMap<K, V> {
    val new = ConcurrentHashMap<K, V>()
    new.putAll(this)
    new.putAll(other)
    return new
}

inline fun <R> runCatchingWithPlayer(player: Player, block: () -> R): Result<R> {
    val res = runCatching(block)
    res.fold(
        onSuccess = { obj ->
            return Result.success(obj)
        },
        onFailure = { err ->
            val mc = err.macrocosm
            player.sendMessage(mc.component)
            return Result.failure(mc)
        }
    )
}

inline fun <R> runCatchingReporting(player: Player? = null, block: () -> R): Result<R> {
    val res = runCatching(block)
    res.fold(
        onSuccess = { obj ->
            return Result.success(obj)
        },
        onFailure = { err ->
            val mc = err.macrocosm
            player?.sendMessage(mc.component)
            report("${mc.code}: ${mc.message}", nullFn())
            return Result.failure(mc)
        }
    )
}

inline fun <reified T> fromJson(str: String): T? = if (str == "NULL") null else GSON.fromJson<T>(str, typetoken<T>())
fun <T> toJson(obj: T): String = GSON.toJson(obj)
inline fun <reified T> typetoken(): java.lang.reflect.Type = object : TypeToken<T>() {}.type

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase(Locale.getDefault())
}

inline fun <reified K : Any?, reified V : Any?> Iterable<K>.associateWithHashed(producer: FnArgRet<K, V>): HashMap<K, V> {
    val result = hashMapOf<K, V>()
    return associateWithTo(result, producer)
}

inline fun <reified T : Any?> ignoring(ele: T): FnArgRet<Any?, T> = { ele }
inline fun <reified T : Any?> producer(ele: T): FnRet<T> = { ele }
inline fun <reified T : Any?> ignoringProducer(crossinline producer: FnRet<T>): FnArgRet<Any?, T> = { producer() }

inline fun <reified T : Any?> T.repeated(n: Int): Array<T> = Array(n) { this }

@OptIn(ExperimentalContracts::class)
inline fun walkDataResources(vararg path: String, block: (Path) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.UNKNOWN)
    }

    val input = Macrocosm.javaClass.classLoader.getResource("data")!!.toURI()
    val fs = try {
        FileSystems.newFileSystem(input, hashMapOf<String, String>())
    } catch (e: FileSystemAlreadyExistsException) {
        FileSystems.getFileSystem(input)
    }
    val mut = path.toMutableList()
    val first = mut.removeFirstOrNull() ?: return
    for (file in PackProvider.enumerateEntries(fs.getPath(first, *mut.toTypedArray()))) {
        block(file)
    }
}

inline fun Vector.advanceInstantly(loc: Location, mod: Float, times: Int, fn: (Location) -> Unit) {
    val pos = loc.clone()
    val mul = this.multiply(mod)
    for (i in 0..times) {
        pos.add(mul)
        fn(pos)
    }
}

inline fun <K, V, O> HashMap<K, V>.mapPaired(fn: (Pair<K, V>) -> O): MutableList<O> {
    val aggregator = mutableListOf<O>()
    for (entry in this.entries) {
        aggregator.add(fn(entry.toPair()))
    }
    return aggregator
}

infix fun Double.insteadOfNaN(other: Double): Double {
    return if(this.isNaN())
        other
    else this
}

fun Duration.toFancyString(): String {
    val days = this.toDaysPart()
    val hours = this.toHoursPart()
    val mins = this.toMinutesPart()
    val secs = this.toSecondsPart()
    val endStr = StringBuilder()

    if (days > 0) {
        endStr.append("${days}d ")
    }
    if (hours > 0) {
        endStr.append("${hours}h ")
    }
    if (mins > 0) {
        endStr.append("${mins}m ")
    }
    if (secs > 0) {
        endStr.append("${secs}s ")
    }
    return endStr.toString()
}

fun superCritMod(p: MacrocosmPlayer): Float = 1 + (p.stats()!!.critChance / 100f)

fun <T : Any> T.equalsAny(vararg possible: T): Boolean {
    return possible.any { it == this }
}

inline fun <reified T> Collection<T>.certain(predicate: (T) -> Boolean, operator: (T) -> Unit) {
    for (value in this) {
        if (predicate(value))
            operator(value)
    }
}


inline fun runNTimes(
    times: Long,
    period: Long,
    noinline finishCallback: () -> Unit = { },
    crossinline runnable: (KSpigotRunnable) -> Unit
): KSpigotRunnable {
    var counter = 0
    val max = if (times <= 0) 1 else times
    return task(period = period) {
        runnable(it)

        if (it.isCancelled) {
            finishCallback()
            return@task
        }
        counter++
        if (counter >= max) {
            it.cancel()
            finishCallback()
            return@task
        }
    }!!
}

fun unreachable(): Nothing {
    throw IllegalStateException("Unreachable condition reached!")
}

fun todo(message: String = "Not finished yet!"): Nothing {
    throw IllegalStateException("TODO reached: $message")
}

fun String.stripTags() = MiniMessage.miniMessage().stripTags(this)

fun renderBar(
    percentage: Float,
    notches: Int,
    from: TextColor,
    to: TextColor = from,
    background: TextColor = NamedTextColor.GRAY
): String {
    val tiles = min(ceil(percentage * (notches + (notches / 3))).roundToInt(), notches)
    return "<gradient:${from.asHexString()}:${to.asHexString()}>" + "-".repeat(tiles) + "</gradient><${background.asHexString()}>" + "-".repeat(
        notches - tiles
    )
}

fun ticksToTime(ticks: Long): String {
    var hours = (ticks / 1000) + 6
    var minutes = (ticks % 1000) / (1000 / 60)
    while (minutes >= 60) {
        hours++
        minutes -= 60
    }
    val suffix = if (hours >= 24) {
        hours -= 24
        "AM"
    } else if (hours > 12) {
        hours -= 12
        "PM"
    } else "AM"
    return (if (hours < 10) "0" else "") + hours + ":" + (if (minutes < 10) "0" else "") + minutes + suffix
}

fun <K, V> ConcurrentHashMap<K, ConcurrentLinkedQueue<V>>.setOrAppend(key: K, value: V): Int {
    return if (this.containsKey(key)) {
        val v = this[key]!!
        v.add(value)
        this[key] = v
        v.indexOf(value)
    } else {
        this[key] = ConcurrentLinkedQueue<V>().apply { add(value) }
        0
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
    if (this.exists())
        this.delete()
    this.createNewFile()
}

fun File.recreateDir() {
    if (this.exists())
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

fun threadNoinline(
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

inline fun <reified T> Collection<T>.pad(demand: Int, value: T): MutableCollection<T> {
    val neededToPad = demand - size
    val eachSide = floor(neededToPad / 2f).roundToInt()
    val list = mutableListOf<T>()
    val repeated = value.repeated(eachSide)
    list.addAll(repeated)
    list.addAll(this)
    list.addAll(repeated)
    return list
}

inline fun <reified T> Collection<T>.padNullsForward(demand: Int): MutableCollection<T?> {
    if (size >= demand) {
        val new = mutableListOf<T?>()
        new.addAll(this)
        return new
    }
    val required = demand - size
    val new = mutableListOf<T?>()
    new.addAll(this)
    new.addAll(List(required) { null })
    return new
}

inline fun <reified T> Collection<T>.padForward(demand: Int, value: T): MutableCollection<T> {
    if (size >= demand) {
        val new = mutableListOf<T>()
        new.addAll(this)
        return new
    }
    val required = demand - size
    val new = mutableListOf<T>()
    new.addAll(this)
    new.addAll(List(required) { value })
    return new
}

fun Random.nextSignedDouble(): Double {
    return if (nextBoolean()) nextDouble() else -nextDouble()
}

inline fun <reified K, reified V> multimap(): Multimap<K, V> = ArrayListMultimap.create()

fun allOf(vararg conditions: Boolean): Boolean = conditions.all { it }
fun anyOf(vararg conditions: Boolean): Boolean = conditions.any { it }
inline fun <reified T> anyNull(vararg nullables: T?): Boolean = nullables.any { it == null }
inline fun <reified T> allNull(vararg nullables: T?): Boolean = nullables.all { it == null }

fun String.containsAny(vararg possibles: String): Boolean = possibles.any { contains(it) }

fun Player.giveOrDrop(item: ItemStack) {
    if (inventory.firstEmpty() == -1) world.dropItem(location, item) else inventory.addItem(item)
}
