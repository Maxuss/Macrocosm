package space.maxus.macrocosm.zone

import com.google.common.base.Predicates
import org.bukkit.Location
import org.bukkit.block.Biome
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.id

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
        }

        fun init() {
            Registry.ZONE.delegateRegistration(values().map { id(it.name.lowercase()) to it.zone })
            Registry.ZONE.delegateRegistration(allowedBiomes.map { id(it.key.name.lowercase()) to BiomeZone(id(it.key.name.lowercase()), it.value, it.key) })
        }
    }

}

class BiomeZone(id: Identifier, name: String, private val biome: Biome): Zone(id, name) {
    override fun contains(location: Location): Boolean {
        return location.block.biome == biome
    }
}
