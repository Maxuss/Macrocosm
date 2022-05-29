package space.maxus.macrocosm.fishing.predicates

import org.bukkit.entity.FishHook
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.zone.Zone
import java.util.function.Predicate

class FishPredicate(private val executor: (MacrocosmPlayer, Zone, FishHook) -> Boolean): Predicate<Triple<MacrocosmPlayer, Zone, FishHook>> {
    override fun test(t: Triple<MacrocosmPlayer, Zone, FishHook>): Boolean {
        return executor(t.first, t.second, t.third)
    }
}
