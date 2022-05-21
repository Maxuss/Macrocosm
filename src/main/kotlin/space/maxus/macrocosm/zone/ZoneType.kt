package space.maxus.macrocosm.zone

import com.google.common.base.Predicates
import org.bukkit.Location
import org.bukkit.block.Biome
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class ZoneType(val zone: Zone) {
    // these only contains special zones, others are just biomes mostly
    NONE(Zone.impl(id("null"), "<dark_gray>None", Predicates.alwaysTrue())),
    OVERWORLD(Zone.impl(id("overworld"), "<green>Overworld") { return@impl it.world.environment == org.bukkit.World.Environment.NORMAL }),
    NETHER(Zone.impl(id("nether"), "<red>Nether") { return@impl it.world.environment == org.bukkit.World.Environment.NETHER }),
    THE_END(Zone.impl(id("the_end"), "<dark_purple>The End") { return@impl it.world.environment == org.bukkit.World.Environment.THE_END }),

    ;
    companion object {
        private val allowedBiomes: HashMap<Biome, String> = hashMapOf(
            Biome.FOREST to "<green>Forest",
            Biome.BIRCH_FOREST to "<gray>Bi<dark_gray>rch <gray>Fore<dark_gray>st",
            Biome.DARK_FOREST to "<dark_green>Dark Forest",
            Biome.CRIMSON_FOREST to "<red>Crimson Forest",
            Biome.WARPED_FOREST to "<aqua>Warped Forest",
            Biome.WINDSWEPT_FOREST to "<gold>Windswept Forest",
            Biome.BADLANDS to "<gold>Badlands",
            Biome.NETHER_WASTES to "<red>Nether Wastes",
            Biome.OCEAN to "<blue>Ocean",
            Biome.TAIGA to "<dark_gray>Taiga",
            Biome.SNOWY_TAIGA to "<white>Snowy Taiga",
            Biome.SNOWY_PLAINS to "<white>Snowy Plains",
            Biome.JUNGLE to "<aqua>Jungle",
            Biome.BAMBOO_JUNGLE to "<aqua>Bamboo Jungle",
            Biome.SPARSE_JUNGLE to "<aqua>Sparse Jungle",
            Biome.LUSH_CAVES to "<green>Lush Caves",
            Biome.DRIPSTONE_CAVES to "<gray>Dripstone Caves",
            Biome.ICE_SPIKES to "<aqua>Ice Spikes",
            Biome.DESERT to "<yellow>Desert",
            Biome.BEACH to "<yellow>Beach",
            Biome.END_BARRENS to "<light_purple>End Barrens",
            Biome.MEADOW to "<green>Meadow",
            Biome.SAVANNA to "<gold>Savanna",
            Biome.WINDSWEPT_HILLS to "<gold>Windswept Hills"
        )
        private fun initBiomes() {
            val pool = Threading.pool()

            for((biome, name) in allowedBiomes) {
                pool.execute {
                    val id = id(biome.name.lowercase())
                    ZoneRegistry.register(id, BiomeZone(id, name, biome))
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }

        fun init() {
            Threading.start("Zone Registry Daemon") {
                info("Starting Zone Registry daemon...")

                val pool = Threading.pool()
                for (zone in values()) {
                    pool.execute {
                        val id = id(zone.name.lowercase())
                        ZoneRegistry.register(id, zone.zone)
                    }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")

                info("Successfully registered ${values().size} zones")
            }
            Threading.start("Biome-bound Zone Daemon") {
                info("Starting Biome-bound Zone Registry daemon...")

                initBiomes()

                info("Successfully registered ${allowedBiomes.size} biome-bound zones")
            }
        }
    }

}

class BiomeZone(id: Identifier, name: String, val biome: Biome): Zone(id, name) {
    override fun contains(location: Location): Boolean {
        return location.block.biome == biome
    }
}
