package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.recipes.types.shapedRecipe
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class RecipeValue(private val recipe: SbRecipe) {
    ASPECT_OF_THE_END(shapedRecipe("aspect_of_the_end", ItemValue.ASPECT_OF_THE_END.item, listOf(" P "," P "," D "),
    'P' to (id("enchanted_ender_pearl") to 16), 'D' to (id("enchanted_diamond") to 1)))
    ;

    companion object {
        fun init() {
            Threading.start {
                val pool = Threading.pool()

                for (recipe in values().toList().parallelStream()) {
                    pool.execute { RecipeRegistry.register(id(recipe.name.lowercase()), recipe.recipe) }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")
            }
        }
    }
}
