package space.maxus.macrocosm.data

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

/**
 * An accessor that allows for easier FS manipulations
 */
object Accessor {
    /**
     * True if Macrocosm has not started before
     */
    var firstStart: Boolean = false; private set
    private lateinit var path: Path

    /**
     * Returns the path relative to the current FS path (usually $server_path/macrocosm)
     */
    fun access(relative: String): Path {
        return path.resolve(relative)
    }

    /**
     * Reads the file data only if file exists, and wraps the result in [ConditionalValueCallback]
     */
    fun readIfExists(relative: String): ConditionalValueCallback<String> {
        return ConditionalValueCallback { access(relative).let { if (it.exists()) it.toFile().readText() else null } }
    }

    /**
     * Overwrites the file, deleting it if it exists
     */
    fun overwrite(file: String, value: String) {
        val accessed = access(file)
        accessed.recreateFile()
        accessed.writeText(value)
    }

    /**
     * Overwrites the file with the provided writer, deleting it if it exists
     */
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

    /**
     * Initializes the accessor.
     *
     * This operation is **NOT Thread Safe**, so it must be called
     * in synchronous environment
     */
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
