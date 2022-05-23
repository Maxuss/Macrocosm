package space.maxus.macrocosm.pack

import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import kotlin.io.path.Path

object EmbedPackProvider {
    private val FILE = Path(Macrocosm.dataFolder.absolutePath, "pack.zip").toFile()

    fun prepare() {
        Threading.start {
            val res = Macrocosm.getResource("pack.zip")!!.readBytes()
            FILE.createNewFile()
            FILE.writeBytes(res)
        }
    }
}
