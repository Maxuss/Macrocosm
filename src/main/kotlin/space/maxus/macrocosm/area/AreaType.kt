package space.maxus.macrocosm.area

import com.google.common.base.Predicates
import org.bukkit.Location
import org.bukkit.block.Biome
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.id

enum class AreaType(val area: Area) {
    // these only contains special zones, others are just biomes mostly
    NONE(Area.impl(id("null"), "<dark_gray>None", Predicates.alwaysTrue())),
    OVERWORLD(
        Area.impl(
            id("overworld"),
            "<green>Overworld"
        ) { return@impl it.world.environment == org.bukkit.World.Environment.NORMAL }),
    NETHER(
        Area.impl(
            id("nether"),
            "<red>Nether"
        ) { return@impl it.world.environment == org.bukkit.World.Environment.NETHER }),
    THE_END(
        Area.impl(
            id("the_end"),
            "<dark_purple>The End"
        ) { return@impl it.world.environment == org.bukkit.World.Environment.THE_END }),

    ;
}

class BiomeArea(id: Identifier, name: String, private val biome: Biome) : Area(id, name) {
    override fun contains(location: Location): Boolean {
        return location.block.biome == biome
    }
}
