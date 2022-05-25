package space.maxus.macrocosm.pack

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.text.comp
import java.io.File
import java.nio.file.Path


object PackProvider: Listener {
    private var RESOURCE_PACK_LINK: String? = null
    private var RESOURCE_PACK_HASH: String = "null"

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(e: PlayerJoinEvent) {
        if(RESOURCE_PACK_LINK == null) {
            e.player.kick(comp("<red>Macrocosm is loading, please wait!"))
            return
        }
        e.player.setResourcePack(RESOURCE_PACK_LINK!!, RESOURCE_PACK_HASH, true, comp("<light_purple>Macrocosm <aqua>requires</aqua> you to use this resource pack.\n<red>Otherwise it may not work correctly!"))
    }

    fun init() {
        val path = Path.of(System.getProperty("user.dir"), "macrocosm")
        RESOURCE_PACK_HASH = path.resolve("hashfile.sha").toFile().readText()
        val oldHash = path.resolve("hashfile.sha.old").toFile()
        if(!oldHash.exists()) {
            oldHash.createNewFile()
            oldHash.writeText("null")
        }

        if(oldHash.readText() != RESOURCE_PACK_HASH) {
            // looks like hash changed, we need to update the pack
            oldHash.writeText(RESOURCE_PACK_HASH)
            path.resolve("pack.old").toFile().delete()
            path.resolve("pack.old").toFile().createNewFile()

            Threading.runAsyncRaw {
                // running upload asynchronously
                val key = Macrocosm.config.getString("dropbox-api")!!
                val link = upload(path.resolve("§5§lMacrocosm §d§lPack.zip").toFile(), key).replace("dropbox.com", "dl.dropboxusercontent.com")
                RESOURCE_PACK_LINK = link
                path.resolve("pack.old").toFile().writeText(link)
            }
        } else {
            RESOURCE_PACK_LINK = path.resolve("pack.old").toFile().readText()
        }
    }

    private fun upload(file: File, accessToken: String): String {
        val config = DbxRequestConfig.newBuilder("Macrocosm/Pack-Uploader").build()
        val client = DbxClientV2(config, accessToken)

        val stream = file.inputStream()
        val meta = client.files()
            .uploadBuilder("/${file.name}")
            .withMode(WriteMode.OVERWRITE)
            .uploadAndFinish(stream, Long.MAX_VALUE)
        Macrocosm.logger.info("Finished uploading resource pack to server! ID: ${meta.id}")
        return try {
            client.sharing().createSharedLinkWithSettings("/${file.name}").url
        } catch(e: Exception) {
            return "https://www.dl.dropboxusercontent.com/s/yuvxnq7erb41mxq/%C2%A75%C2%A7lMacrocosm%20%C2%A7d%C2%A7lPack.zip?dl=0"
        }
    }
}
