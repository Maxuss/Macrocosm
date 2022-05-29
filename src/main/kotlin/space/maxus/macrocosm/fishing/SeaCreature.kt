package space.maxus.macrocosm.fishing

import org.bukkit.entity.FishHook
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.Chance
import space.maxus.macrocosm.zone.Zone
import java.util.function.Predicate

data class SeaCreature(
    val greeting: String,
    val entity: Identifier,
    val requiredLevel: Int,
    val predicate: Predicate<Triple<MacrocosmPlayer, Zone, FishHook>>,
    override val chance: Double
) :
    Chance
