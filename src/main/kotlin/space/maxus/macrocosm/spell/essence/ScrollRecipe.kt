package space.maxus.macrocosm.spell.essence

import com.google.gson.JsonObject
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.walkDataResources
import kotlin.io.path.readText

data class ScrollRecipe(val requirements: HashMap<EssenceType, Int>, val result: Identifier, val level: Int) {
    companion object {
        fun init() {
            Threading.runAsync("ScrollParser") {
                val pool = Threading.newFixedPool(5)
                info("Starting scroll recipe parser...")

                walkDataResources("data", "scrolls") { file ->
                    val data = GSON.fromJson(file.readText(), JsonObject::class.java)
                    for((key, obj) in data.entrySet()) {
                        pool.execute {
                            Registry.SCROLL_RECIPE.register(Identifier.parse(key), parseSingle(obj.asJsonObject))
                        }
                    }
                }

                pool.shutdown()
            }
        }

        private fun parseSingle(jo: JsonObject): ScrollRecipe {
            val result = Identifier.parse(jo["result"].asString)
            val level = jo["level"].asInt
            val requirements = hashMapOf<EssenceType, Int>()
            jo["requirements"].asJsonArray.forEach { ele ->
                if(ele.isJsonObject) {
                    val obj = ele.asJsonObject
                    requirements[EssenceType.valueOf(obj["type"].asString.uppercase())] = obj["amount"].asInt
                } else {
                    requirements[EssenceType.valueOf(ele.asString.uppercase())] = 1
                }
            }
            return ScrollRecipe(requirements, result, level)
        }
    }
}
