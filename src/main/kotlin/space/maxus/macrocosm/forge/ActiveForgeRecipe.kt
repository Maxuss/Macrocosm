package space.maxus.macrocosm.forge

import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoActiveForgeRecipe
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.io.Serializable
import java.time.Duration
import java.time.Instant

data class ActiveForgeRecipe(val id: Identifier, val startTime: Long) : Serializable,
    MongoConvert<MongoActiveForgeRecipe> {
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

    override val mongo: MongoActiveForgeRecipe
        get() = MongoActiveForgeRecipe(id.toString(), startTime)
}
