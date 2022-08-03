package space.maxus.macrocosm.forge

import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.time.Duration

data class ActiveForgeRecipe(val id: Identifier, val startTime: Long) {
    fun isDoneByNow() {
        val duration = Duration.ofSeconds(Registry.FORGE_RECIPE.find(id).length).toMillis()
        val start = startTime + duration
    }
}
