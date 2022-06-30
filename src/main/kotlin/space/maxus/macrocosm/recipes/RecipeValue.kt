package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.recipes.types.shapedRecipe
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.generic.id
import java.util.concurrent.TimeUnit

enum class RecipeValue(private val recipe: MacrocosmRecipe) {
    ASPECT_OF_THE_END(
        shapedRecipe(
            "aspect_of_the_end", ItemValue.ASPECT_OF_THE_END.item, 1,
            listOf(" P ", " P ", " D "),
            'P' to (id("enchanted_ender_pearl") to 16), 'D' to (id("enchanted_diamond") to 1)
        )
    ),

    ;

    companion object {
        private fun initEnchanted() {
            // todo: enchanted item recipes
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
