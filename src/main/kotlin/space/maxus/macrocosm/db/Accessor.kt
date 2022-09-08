package space.maxus.macrocosm.db

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.util.general.ConditionalValueCallback
import space.maxus.macrocosm.util.recreateFile
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.io.path.*

object Accessor {
    var firstStart: Boolean = false; private set
    private lateinit var path: Path

    fun access(relative: String): Path {
        return path.resolve(relative)
    }

    fun readIfOpen(relative: String): ConditionalValueCallback<String> {
        return ConditionalValueCallback { access(relative).let { if (it.exists()) it.toFile().readText() else null } }
    }

    fun overwrite(file: String, value: String) {
        val accessed = access(file)
        accessed.recreateFile()
        accessed.writeText(value)
    }

    @OptIn(ExperimentalContracts::class)
    inline fun overwrite(file: String, crossinline writer: (DataOutputStream) -> Unit = { }) {
        contract {
            callsInPlace(writer, InvocationKind.EXACTLY_ONCE)
        }

        // we need to drift away from main thread
        // due to I/O operations
        Threading.driftFromMain {
            val accessed = access(file)
            accessed.recreateFile()
            val stream = DataOutputStream(BufferedOutputStream(accessed.outputStream()))
            writer(stream)
            stream.flush()
            stream.close()
        }
    }

    fun init() {
        path = Path.of(System.getProperty("user.dir"), "macrocosm")
        if (path.notExists()) {
            path.createDirectories()
        }
        val lockfile = path.resolve(".lockfile")
        firstStart = !lockfile.exists()
        if (firstStart) {
            access("snapshots").createDirectories()
            lockfile.createFile()
        }
    }
}
