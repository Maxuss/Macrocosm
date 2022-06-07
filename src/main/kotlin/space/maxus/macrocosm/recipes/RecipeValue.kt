package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.recipes.types.shapedRecipe
import space.maxus.macrocosm.recipes.types.shapelessRecipe
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class RecipeValue(private val recipe: MacrocosmRecipe) {
    ASPECT_OF_THE_END(
        shapedRecipe(
            "aspect_of_the_end", ItemValue.ASPECT_OF_THE_END.item, listOf(" P ", " P ", " D "),
            'P' to (id("enchanted_ender_pearl") to 16), 'D' to (id("enchanted_diamond") to 1)
        )
    )
    ;

    companion object {
        private fun initEnchanted() {
            // preventing a memory leak
            val pool = Threading.newFixedPool(12)

            for (mat in ItemValue.allowedEnchantedMats.toList().parallelStream()) {
                pool.execute {
                    val id = id("enchanted_${mat.lowercase()}")
                    var result = Registry.ITEM.findOrNull(id)
                    while(result == null) {
                        Macrocosm.logger.warning("Thread racing! Tried to find $id in item registry, but it was not yet registered!")
                        Thread.sleep(1000)
                        result = Registry.ITEM.findOrNull(id)
                    }
                    val pair = id("minecraft", mat.lowercase()) to 32
                    Registry.RECIPE.register(
                        id("enchanted_${mat.lowercase()}"), shapelessRecipe(
                            "enchanted_${mat.lowercase()}",
                            result,

                            pair,
                            pair,
                            pair,
                            pair,
                            pair
                        )
                    )
                }
            }

            pool.shutdown()
        }

        private fun initBasic() {
            val pool = Threading.newCachedPool()

            for (recipe in values().toList().parallelStream()) {
                pool.execute { Registry.RECIPE.register(id(recipe.name.lowercase()), recipe.recipe) }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }

        fun init() {
            Threading.runAsync("Basic Recipe Registry", true) {
                info("Starting Basic Recipe daemon...")
                initBasic()
            }
            Threading.runAsync("Enchanted Recipe Registry", true) {
                info("Starting Enchanted Recipe daemon...")
                initEnchanted()
            }
        }
    }
}
