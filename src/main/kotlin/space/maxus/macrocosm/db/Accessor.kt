package space.maxus.macrocosm.db

import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.util.general.ConditionalValueCallback
import space.maxus.macrocosm.util.recreateFile
import java.nio.ByteBuffer
import java.nio.file.Path
import kotlin.io.path.*

object Accessor {
    var firstStart: Boolean = false; private set
    private lateinit var path: Path; private set

    fun access(relative: String): Path {
        return path.resolve(relative)
    }

    fun readIfOpen(relative: String): ConditionalValueCallback<String> {
        return ConditionalValueCallback { access(relative).let { if (it.exists()) it.toFile().readText() else null } }
    }

    fun overwrite(relative: String, value: String) {
        val file = access(relative)
        file.recreateFile()
        file.writeText(value)
    }

    fun init() {
        path = Path.of(System.getProperty("user.dir"), "macrocosm")
        if (path.notExists()) {
            path.createDirectories()
        }
        val lockfile = path.resolve(".lockfile")
        firstStart = !lockfile.exists()
        if (firstStart) {
            access("api.conf").writeText(
                Charsets.UTF_8.decode(
                    ByteBuffer.wrap(
                        Macrocosm.getResource("application.conf")!!.readAllBytes()
                    )
                ).toString()
            )
            lockfile.createFile()
        }
    }
}
