package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id
import java.util.concurrent.TimeUnit

enum class RecipeValue(private val recipe: MacrocosmRecipe) {
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
            Threading.contextBoundedRunAsync("Basic Recipe Registry", true) {
                info("Starting Basic Recipe daemon...")
                initBasic()
            }
            Threading.contextBoundedRunAsync("Enchanted Recipe Registry", true) {
                info("Starting Enchanted Recipe daemon...")
                initEnchanted()
            }
        }
    }
}
