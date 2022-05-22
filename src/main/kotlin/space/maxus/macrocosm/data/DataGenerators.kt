package space.maxus.macrocosm.data

import org.slf4j.LoggerFactory
import space.maxus.macrocosm.registry.Registry
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

object DataGenerators {
    @JvmStatic
    fun registries() {
        val logger = LoggerFactory.getLogger("space.maxus.macrocosm.data.Main")
        logger.info("Starting data generator")
        val dirPath = Path(System.getProperty("user.dir"), "MacrocosmDatagen")
        if(dirPath.toFile().exists())
            dirPath.toFile().deleteRecursively()
        dirPath.createDirectories()

        for ((id, reg) in Registry.iter()) {
            reg.dumpToFile(dirPath.resolve("${id}_dump.json"))
        }
    }
}
