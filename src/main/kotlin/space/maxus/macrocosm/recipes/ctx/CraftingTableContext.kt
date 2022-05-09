package space.maxus.macrocosm.recipes.ctx

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.recipes.Ingredient
import space.maxus.macrocosm.recipes.RecipeContext

data class CraftingTableContext(val items: List<MacrocosmItem>): RecipeContext {
    override fun ingredientAt(slot: Int): Ingredient {
        return items[slot]
    }

    override fun mostImportantItem(): MacrocosmItem? {
        return items.filter { it.type != ItemType.OTHER || it.reforge != null }.sortedBy { it.rarity.ordinal + it.enchantments.size }.lastOrNull()
    }
}
