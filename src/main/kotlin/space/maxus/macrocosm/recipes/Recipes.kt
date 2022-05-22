package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.registry.Identifier

object Recipes {
    fun using(item: Identifier): List<MacrocosmRecipe> {
        return Registry.RECIPE.iter().values.filter { it.ingredients().any { l -> l.any { ing -> ing.first == item } } }
    }
}
