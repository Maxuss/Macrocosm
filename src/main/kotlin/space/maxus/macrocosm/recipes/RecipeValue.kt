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
            "aspect_of_the_end", ItemValue.ASPECT_OF_THE_END.item, 1,
            listOf(" P ", " P ", " D "),
            'P' to (id("enchanted_ender_pearl") to 16), 'D' to (id("enchanted_diamond") to 1)
        )
    ),

    // slayers
    // zombie
    REVENANT_VISCERA(
        shapelessRecipe(
            "revenant_viscera", ItemValue.REVENANT_VISCERA.item, 4,
            *List(4) { id("revenant_viscera") to 32 }.toTypedArray(),
            id("enchanted_string") to 32
        )
    ),

    REVENANT_INNARDS(
        shapelessRecipe(
            "revenant_innards", ItemValue.REVENANT_INNARDS.item, 16,
            id("rancid_flesh") to 16,
            *List(4) { id("foul_flesh") to 32 }.toTypedArray(),
        )
    ),

    REAPER_MASK(
        shapedRecipe(
            "reaper_mask", ItemValue.REAPER_MASK.item, 1,
            listOf(
                "FHF",
                "VDV",
                "FVF"
            ),
            'F' to (id("foul_flesh") to 16),
            'H' to (id("beheaded_horror") to 1),
            'V' to (id("revenant_viscera") to 32),
            'D' to (id("enchanted_diamond_block") to 8)
        )
    ),

    ENTOMBED_MASK(
        shapedRecipe(
            "entombed_mask", ItemValue.ENTOMBED_MASK.item, 1,
            listOf(
                "NBN",
                "IRI",
                "NIN"
            ),
            'N' to (id("enchanted_netherite_ingot") to 16),
            'B' to (id("decaying_brain") to 1),
            'I' to (id("revenant_innards") to 32),
            'R' to (id("reaper_mask") to 1)
        )
    ),

    WARDENS_HELMET(
        shapedRecipe(
            "wardens_helmet", ItemValue.WARDENS_HELMET.item, 1,
            listOf(
                "NHN",
                "IBI"
            ),
            'H' to (id("wardens_heart") to 1),
            'N' to (id("enchanted_netherite_block") to 1),
            'I' to (id("revenant_innards") to 32),
            'B' to (id("beheaded_horror") to 1)
        )
    ),


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
                            1,

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
