package space.maxus.macrocosm.pack

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.recreateFile
import java.io.BufferedInputStream
import java.io.File
import java.math.BigInteger
import java.nio.file.FileSystems
import java.nio.file.Path
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.outputStream


object PackProvider: Listener {
    private var RESOURCE_PACK_LINK: String? = null
    private var RESOURCE_PACK_HASH: String = "null"

    const val PACK_NAME = "§5§lMacrocosm §d§lPack.zip"
    private const val BUFFER_SIZE = 4096

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        if(RESOURCE_PACK_LINK == null) {
            e.player.kick(comp("<red>Macrocosm is loading, please wait!"))
            return
        }
        e.player.setResourcePack(RESOURCE_PACK_LINK!!, RESOURCE_PACK_HASH, true, comp("<light_purple>Macrocosm <aqua>requires</aqua> you to use this resource pack.\n<red>Otherwise it may not work correctly!"))
    }

    fun init() {
        // running upload asynchronously
        Threading.runAsyncRaw {
            // compiling
            compile()

            // uploading stuff
            val path = Path.of(System.getProperty("user.dir"), "macrocosm")
            val accessToken = Macrocosm.config.getString("pack.access-token")!!
            val refreshToken = Macrocosm.config.getString("pack.refresh-token")!!
            val appKey = Macrocosm.config.getString("pack.app-key")
            val appSecret = Macrocosm.config.getString("pack.app-secret")
            val link = upload(path.resolve(PACK_NAME).toFile(), DbxCredential(accessToken, 14400, refreshToken, appKey, appSecret)).replace(
                "dropbox.com",
                "dl.dropboxusercontent.com"
            )
            Macrocosm.logger.info("Finished uploading resource pack to server! Link: $link")
            RESOURCE_PACK_LINK = link
        }
    }

    private fun enumerateEntries(dir: Path): List<Path> {
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
        val fs = FileSystems.newFileSystem(input, hashMapOf<String, String>())

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

        zip.close()
        zip.flush()
        Macrocosm.logger.info("Finished zipping resource pack, calculating hash...")

        // calculating sha1 hash
        val packFile = Path.of(System.getProperty("user.dir"), "macrocosm").resolve(PACK_NAME)
        val bytes = packFile.toFile().inputStream().readAllBytes()
        val hasher = MessageDigest.getInstance("SHA-1")
        val digest = hasher.digest(bytes)
        val hash = BigInteger(1, digest).toString(16)
        Macrocosm.logger.info("Resource pack SHA-1 hash: $hash")

        RESOURCE_PACK_HASH = hash
    }

    private fun upload(file: File, credential: DbxCredential): String {
        val config = DbxRequestConfig.newBuilder("Macrocosm/Pack-Uploader").build()
        val client = DbxClientV2(config, credential)
        if(credential.aboutToExpire())
            client.refreshAccessToken()

        val stream = file.inputStream()
        client.files()
            .uploadBuilder("/${file.name}")
            .withMode(WriteMode.OVERWRITE)
            .uploadAndFinish(stream, Long.MAX_VALUE)
        return try {
            client.sharing().createSharedLinkWithSettings("/${file.name}").url
        } catch(e: Exception) {
            return "https://www.dl.dropboxusercontent.com/s/yuvxnq7erb41mxq/%C2%A75%C2%A7lMacrocosm%20%C2%A7d%C2%A7lPack.zip?dl=0"
        }
    }
}
