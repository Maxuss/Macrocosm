package space.maxus.macrocosm.achievement.json

import com.google.gson.JsonObject
import space.maxus.macrocosm.achievement.Achievement
import space.maxus.macrocosm.achievement.AchievementRarity
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.walkDataResources
import java.util.concurrent.TimeUnit
import kotlin.io.path.readText

/**
 * A JSON Achievement parser
 */
object AchievementParser {
    /**
     * Reads all JSON achievements
     */
    fun init() {
        val pool = Threading.newFixedPool(5)

        walkDataResources("data", "achievements") { file ->
            val data = GSON.fromJson(file.readText(), JsonObject::class.java)
            for ((key, obj) in data.entrySet()) {
                pool.execute {
                    val id = Identifier.parse(key)
                    Registry.ACHIEVEMENT.register(id, parseAndPrepare(id, obj.asJsonObject))
                }
            }
        }

        pool.shutdown()
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
    }

    private fun parseAndPrepare(id: Identifier, obj: JsonObject): Achievement {
        val name = obj["name"].asString
        val description = obj["description"].asString
        val exp = if(obj.has("exp")) obj["exp"].asInt else 10
        val rarity = if(obj.has("rarity")) AchievementRarity.valueOf(obj["rarity"].asString.uppercase()) else AchievementRarity.BASIC
        return Achievement(id.path, name, description, exp, rarity)
    }
}
