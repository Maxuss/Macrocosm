package space.maxus.macrocosm.forge

import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.io.Serializable
import java.time.Duration
import java.time.Instant

data class ActiveForgeRecipe(val id: Identifier, val startTime: Long) : Serializable {
    fun isDoneByNow(): Boolean {
        val duration = Duration.ofSeconds(Registry.FORGE_RECIPE.find(id).length).toMillis()
        val requiredStamp = startTime + duration
        return requiredStamp <= Instant.now().toEpochMilli()
    }

    fun leftTime(): Duration {
        val duration = Duration.ofSeconds(Registry.FORGE_RECIPE.find(id).length).toMillis()
        val requiredStamp = startTime + duration
        return Duration.ofMillis(requiredStamp - Instant.now().toEpochMilli())
    }
}
