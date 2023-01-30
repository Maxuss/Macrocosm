package space.maxus.macrocosm.fishing.predicates

import org.bukkit.entity.FishHook
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.area.Area
import java.util.function.Predicate

class FishPredicate(private val executor: (MacrocosmPlayer, Area, FishHook) -> Boolean) :
    Predicate<Triple<MacrocosmPlayer, Area, FishHook>> {
    override fun test(t: Triple<MacrocosmPlayer, Area, FishHook>): Boolean {
        return executor(t.first, t.second, t.third)
    }
}
