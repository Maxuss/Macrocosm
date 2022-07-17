package space.maxus.macrocosm.recipes

import com.google.gson.JsonObject
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.pack.PackProvider
import space.maxus.macrocosm.recipes.types.ShapedRecipe
import space.maxus.macrocosm.recipes.types.ShapelessRecipe
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.GSON
import java.nio.file.FileSystemAlreadyExistsException
import java.nio.file.FileSystems
import kotlin.io.path.readText

object RecipeParser {
    fun init() {
        Threading.runAsync(name = "RecipeParser") {
            val pool = Threading.newFixedPool(5)
            info("Starting recipe parser...")

            val input = this.javaClass.classLoader.getResource("data")!!.toURI()
            val fs = try {
                FileSystems.newFileSystem(input, hashMapOf<String, String>())
            } catch (e: FileSystemAlreadyExistsException) {
                FileSystems.getFileSystem(input)
            }
            for(file in PackProvider.enumerateEntries(fs.getPath("data", "recipes"))) {
                info("Converting recipes from ${file.fileName}...")
                val data = GSON.fromJson(file.readText(), JsonObject::class.java)
                for((key, obj) in data.entrySet()) {
                    pool.execute {
                        val id = Identifier.parse(key)
                        Registry.RECIPE.register(id, parse(id, obj.asJsonObject))
                    }
                }
            }
            pool.shutdown()
        }
    }

    fun parse(id: Identifier, j: JsonObject): MacrocosmRecipe {
        val result = if(j.has("result")) Identifier.parse(j.get("result").asString) else id
        val amount = if(j.has("amount")) j.get("amount").asInt else 1
        if(j.has("pattern")) {
            val matrix = j.get("pattern").asJsonArray.map { it.asString }
            val map = hashMapOf<Char, Pair<Identifier, Int>>()
            for((str, i) in j.getAsJsonObject("matrix").entrySet()) {
                val ch = str.toCharArray().first()
                if(i.isJsonObject) {
                    val jo = i.asJsonObject
                    map[ch] = Identifier.parse(jo.get("item").asString) to jo.get("amount").asInt
                } else {
                    map[ch] = Identifier.parse(i.asString) to 1
                }
            }
            return ShapedRecipe(id, matrix, map, Registry.ITEM.find(id), amount)
        } else {
            val ingredients = j.getAsJsonArray("ingredients")
            val ings = mutableListOf<Pair<Identifier, Int>>()
            for(i in ingredients) {
                if(i.isJsonObject) {
                    val jo = i.asJsonObject
                    ings.add(Identifier.parse(jo.get("item").asString) to jo.get("amount").asInt)
                } else {
                    ings.add(Identifier.parse(i.asString) to 1)
                }
            }
            return ShapelessRecipe(id, ings, Registry.ITEM.find(result), amount)
        }
    }
}
