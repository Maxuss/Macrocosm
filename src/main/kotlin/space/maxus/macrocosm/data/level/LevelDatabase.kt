package space.maxus.macrocosm.data.level

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.data.Accessor
import space.maxus.macrocosm.util.data.SemanticVersion
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists

object LevelDatabase {
    private val adapters: ConcurrentLinkedQueue<LevelDbAdapter> = ConcurrentLinkedQueue()

    private val backupPath by lazy {
        val os = System.getProperty("os.name").lowercase()
        if(os.contains("linux") || os.contains("mac")) {
            // Linux or OSX
            val path = File("${System.getProperty("user.home")}/.macrocosm")
            if(!path.exists())
                path.mkdirs()
            return@lazy path.resolve("leveldb")
        }
        // Windows
        val path = File("${System.getenv("APPDATA")}\\Macrocosm")
        if(!path.exists())
            path.mkdirs()
        return@lazy path.resolve("leveldb")
    }

    private val localPath = Accessor.access("leveldb")

    fun backup() {
        if(!localPath.exists())
            return
        if(backupPath.exists() && !backupPath.delete())
            return
        localPath.toFile().copyTo(backupPath)
    }

    fun registerAdapter(adapter: LevelDbAdapter) {
        adapters.add(adapter)
    }

    fun save() {
        val baseCompound = CompoundTag()
        baseCompound.putString("MacrocosmVersion", Macrocosm.version.toString())
        baseCompound.putLong("Timestamp", System.currentTimeMillis())
        for(adapter in adapters) {
            val cmp = CompoundTag()
            adapter.save(cmp)
            baseCompound.put(adapter.name, cmp)
        }
        localPath.deleteExisting()
        NbtIo.writeCompressed(baseCompound, localPath.toFile())
    }

    fun load() {
        if(!localPath.exists())
            return
        val compound = NbtIo.readCompressed(localPath.toFile())
        if(SemanticVersion.fromString(compound.getString("MacrocosmVersion")) != Macrocosm.version)
            Macrocosm.logger.warning("Loading older version of leveldb file. Current version is ${Macrocosm.version}, loading ${compound.getString("MacrocosmVersion")}")
        val loadingPool = Threading.newFixedPool(8)
        for(adapter in adapters) {
            if(compound.contains(adapter.name))
                loadingPool.execute { adapter.load(compound.getCompound(adapter.name)) }
        }
        loadingPool.shutdown()
        loadingPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }
}
