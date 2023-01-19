package space.maxus.macrocosm.data

import org.slf4j.LoggerFactory
import space.maxus.macrocosm.registry.Registry
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

/**
 * A data generator that allows to dump certain data for debugging/info
 */
object DataGenerators {
    /**
     * Dumps all the registry data into the MacrocosmResources folder
     */
    @JvmStatic
    fun registries() {
        val logger = LoggerFactory.getLogger("space.maxus.macrocosm.data.DataGenerators")
        logger.info("Starting data generator")
        val dirPath = Path(System.getProperty("user.dir"), "MacrocosmResources")
        if (dirPath.toFile().exists())
            dirPath.toFile().deleteRecursively()
        dirPath.createDirectories()

        for ((id, reg) in Registry.iter()) {
            reg.dumpToFile(dirPath.resolve("${id}_dump.json"))
        }
    }
}
