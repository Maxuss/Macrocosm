package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object RecipeRegistry {
    val recipes: ConcurrentHashMap<Identifier, SbRecipe> = ConcurrentHashMap(hashMapOf())

    fun <R> register(key: Identifier, recipe: R): R where R : SbRecipe {
        recipes[key] = recipe
        return recipe
    }

    fun using(item: Identifier): List<SbRecipe> {
        return recipes.values.filter { it.ingredients().any { l -> l.any { ing -> ing.first == item } } }
    }

    fun find(key: Identifier) = recipes[key]
    fun nameOf(item: SbRecipe) = recipes.filter { (_, v) -> v == item }.map { (k, _) -> k }.firstOrNull()
}
