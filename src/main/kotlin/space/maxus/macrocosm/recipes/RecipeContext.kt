package space.maxus.macrocosm.recipes

import space.maxus.macrocosm.item.MacrocosmItem

interface RecipeContext {
    fun ingredientAt(slot: Int): Ingredient
    fun mostImportantItem(): MacrocosmItem?
}
