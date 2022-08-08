package space.maxus.macrocosm.db

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.notExists

object Accessor {
    var firstStart: Boolean = false; private set
    private lateinit var path: Path; private set

    fun access(relative: String): Path {
        return path.resolve(relative)
    }

    fun init() {
        path = Path.of(System.getProperty("user.dir"), "macrocosm")
        if (path.notExists()) {
            path.createDirectories()
        }
        val lockfile = path.resolve(".lockfile")
        firstStart = !lockfile.exists()
        if(firstStart)
            lockfile.createFile()
    }
}
