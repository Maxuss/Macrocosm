package space.maxus.macrocosm.pack

import com.google.common.io.BaseEncoding
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.recreateFile
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.nio.file.FileSystems
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.*


object PackProvider: Listener {
    private const val RESOURCE_PACK_LINK: String = "http://127.0.0.1:6060/pack"
    private var RESOURCE_PACK_HASH: ByteArray = ByteArray(1)

    const val PACK_NAME = "§5§lMacrocosm §d§lPack.zip"
    private const val BUFFER_SIZE = 4096

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        e.player.setResourcePack(RESOURCE_PACK_LINK, RESOURCE_PACK_HASH, comp("<light_purple>Macrocosm <aqua>requires</aqua> you to use this resource pack.\n<red>Otherwise it may not work correctly!"), true)
    }

    fun init() {
        // running upload asynchronously
        Threading.runAsyncRaw {
            // compiling
            compile()

            // calculating sha1 hash
            val packFile = Path(System.getProperty("user.dir"), "macrocosm").resolve(PACK_NAME).toFile()
            val bytes = packFile.inputStream().readAllBytes()
            val hasher = MessageDigest.getInstance("SHA-1")
            val digest = hasher.digest(bytes)
            Macrocosm.logger.info("Resource pack SHA-1 hash: ${BaseEncoding.base16().encode(digest)}")

            RESOURCE_PACK_HASH = digest

            // hooking server
            PackServer.hook(packFile)
        }
    }

    fun enumerateEntries(dir: Path): List<Path> {
        if(!dir.isDirectory())
            return listOf(dir)
        val out = mutableListOf<Path>()
        for(entry in dir.listDirectoryEntries()) {
            out.addAll(enumerateEntries(entry))
        }
        return out
    }

    private fun compile() {
        Macrocosm.logger.info("Starting zipping resource pack...")

        // getting resources as a file system, for iteration
        val input = this.javaClass.classLoader.getResource("pack")!!.toURI()
        val fs = FileSystems.getFileSystem(input)

        // creating the zip output pack
        val out = Path.of(System.getProperty("user.dir"), "macrocosm").resolve(PACK_NAME)
        out.recreateFile()

        val zip = ZipOutputStream(out.outputStream())

        // iterating through directories
        for(file in enumerateEntries(fs.getPath("pack"))) {
            val relative = file.toString().replace("pack/", "")

            val entry = ZipEntry(relative)
            zip.putNextEntry(entry)

            // writing to zip file
            val stream = BufferedInputStream(file.inputStream())
            val buffer = ByteArray(BUFFER_SIZE)
            var len: Int
            while (stream.read(buffer).also { len = it } > 0) {
                zip.write(buffer, 0, len)
            }
            zip.closeEntry()
            stream.close()
        }

        // generating extra data
        for((_, generator) in Registry.RESOURCE_GENERATORS.iter()) {
            for((relative, value) in generator.yieldGenerate()) {
                val entry = ZipEntry(relative)
                zip.putNextEntry(entry)

                val stream = ByteArrayInputStream(value.toByteArray(Charsets.UTF_8)).buffered()

                val buffer = ByteArray(BUFFER_SIZE)
                var len: Int
                while (stream.read(buffer).also { len = it } > 0) {
                    zip.write(buffer, 0, len)
                }
                zip.closeEntry()
            }
        }

        zip.close()
        zip.flush()
        Macrocosm.logger.info("Finished zipping resource pack, calculating hash...")
    }

    private fun bytesToHex(arrayBytes: ByteArray): String {
        val stringBuffer = StringBuffer()
        for (i in arrayBytes.indices) {
            stringBuffer.append(
                ((arrayBytes[i].toInt() and 0xff) + 0x100).toString(16)
                    .substring(1)
            )
        }
        return stringBuffer.toString()
    }
}
