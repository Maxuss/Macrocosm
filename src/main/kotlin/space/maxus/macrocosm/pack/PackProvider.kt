package space.maxus.macrocosm.pack

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.logger
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.recreateFile
import java.io.*
import java.math.BigInteger
import java.nio.file.FileSystems
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.*


object PackProvider : Listener {
    private var RESOURCE_PACK_LINK: String = "null"
    private var RESOURCE_PACK_HASH: String = "null"

    const val PACK_NAME = "Macrocosm_Pack.zip"
    private const val BUFFER_SIZE = 4096

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        e.player.setResourcePack(
            RESOURCE_PACK_LINK,
            RESOURCE_PACK_HASH,
            true,
            text("<light_purple>Macrocosm <aqua>requires</aqua> you to use this resource pack.\n<red>Otherwise it may not work correctly!")
        )
    }

    fun init() {
        // running upload asynchronously
        Threading.runAsync {
            // compiling
            compile()

            // calculating sha1 hash
            val packFile = Path(System.getProperty("user.dir"), "macrocosm").resolve(PACK_NAME).toFile()

            val hash = digest(packFile)
            Macrocosm.logger.info("Resource pack MD5 hash: $hash")

            RESOURCE_PACK_HASH = hash

            RESOURCE_PACK_LINK = PackUploader.uploadPack(packFile)
        }
    }

    private fun digest(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        val `is`: InputStream = BufferedInputStream(FileInputStream(file))
        val buffer = ByteArray(8192)
        var read: Int
        return try {
            while (`is`.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
            val md5sum = digest.digest()
            val bigInt = BigInteger(1, md5sum)
            bigInt.toString(16)
        } catch (e: IOException) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                `is`.close()
            } catch (e: IOException) {
                logger.severe("Unable to close input stream for MD5 calculation")
            }
        }
    }

    fun enumerateEntries(dir: Path): List<Path> {
        if (!dir.isDirectory())
            return listOf(dir)
        val out = mutableListOf<Path>()
        for (entry in dir.listDirectoryEntries()) {
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
        for (file in enumerateEntries(fs.getPath("pack"))) {
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
        for ((_, generator) in Registry.RESOURCE_GENERATORS.iter()) {
            for ((relative, value) in generator.yieldGenerate()) {
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
}
