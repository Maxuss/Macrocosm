package space.maxus.macrocosm.zone

import com.google.common.base.Predicates
import org.bukkit.Location
import org.bukkit.block.Biome
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.id

enum class ZoneType(val zone: Zone) {
    // these only contains special zones, others are just biomes mostly
    NONE(Zone.impl(id("null"), "<dark_gray>None", Predicates.alwaysTrue())),
    OVERWORLD(
        Zone.impl(
            id("overworld"),
            "<green>Overworld"
        ) { return@impl it.world.environment == org.bukkit.World.Environment.NORMAL }),
    NETHER(
        Zone.impl(
            id("nether"),
            "<red>Nether"
        ) { return@impl it.world.environment == org.bukkit.World.Environment.NETHER }),
    THE_END(
        Zone.impl(
            id("the_end"),
            "<dark_purple>The End"
        ) { return@impl it.world.environment == org.bukkit.World.Environment.THE_END }),

    ;
}

class BiomeZone(id: Identifier, name: String, private val biome: Biome) : Zone(id, name) {
    override fun contains(location: Location): Boolean {
        return location.block.biome == biome
    }
}
