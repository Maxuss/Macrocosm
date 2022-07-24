package space.maxus.macrocosm.recipes.types

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.recipes.MacrocosmRecipe
import space.maxus.macrocosm.recipes.RecipeContext
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.generic.id

fun shapelessRecipe(
    id: String,
    result: MacrocosmItem,
    amount: Int,
    vararg ingredients: Pair<Identifier, Int>
): MacrocosmRecipe =
    ShapelessRecipe(id(id), ingredients.toList(), result, amount)

class ShapelessRecipe(
    override val id: Identifier,
    private val ingredients: List<Pair<Identifier, Int>>,
    private val result: MacrocosmItem,
    private val amount: Int
) : MacrocosmRecipe {
    @Suppress("KotlinConstantConditions")
    override fun matches(
        player: MacrocosmPlayer,
        inv: Inventory,
        grid: List<ItemStack?>,
        modify: Boolean
    ): Pair<Boolean, HashMap<Int, Pair<ItemStack, Int>>> {
        val cache = hashMapOf<Int, Pair<ItemStack, Int>>()

        val ingredientsCopy = ingredients.toMutableList()
        grid.forEachIndexed { i, item ->
            if (item == null || item.type.isAir)
                return@forEachIndexed

            val mc = item.macrocosm ?: return@forEachIndexed
            val id = mc.id
            val amount = item.amount
            ingredientsCopy.toTypedArray().forEach { pair ->
                val (expectedId, expectedAmount) = pair
                if (expectedId == id && amount >= expectedAmount) {
                    ingredientsCopy.remove(pair)
                    cache[cachedIndexToInventory(i)] = Pair(item, expectedAmount)
                    return@forEachIndexed
                }
            }
        }

        if (ingredientsCopy.isNotEmpty())
            return Pair(false, hashMapOf())

        if (modify) {
            for ((index, pair) in cache) {
                val (item, amount) = pair
                item.amount -= amount
                if (item.amount <= 0)
                    inv.setItem(index, null)
                else inv.setItem(index, item)
            }
        }

        return Pair(true, cache)
    }

    override fun assemble(ctx: RecipeContext, player: MacrocosmPlayer): ItemStack {
        val important = ctx.mostImportantItem() ?: return result.clone().build() ?: ItemStack(Material.AIR)
        val cloned = result.clone()
        important.transfer(cloned)
        return cloned.build() ?: ItemStack(Material.AIR)
    }

    override fun resultItem(): ItemStack {
        return result.clone().build()!!
    }

    override fun resultMacrocosm(): MacrocosmItem {
        return result.clone()
    }

    override fun ingredients(): List<List<Pair<Identifier, Int>>> {
        return listOf(ingredients)
    }

    companion object {
        private val gridIndices = listOf(
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(5, 6, 7)
        )
    }

    private fun cachedIndexToInventory(index: Int): Int = when (index) {
        0, 1, 2 -> index + 10
        3, 4, 5 -> index + 16
        6, 7, 8 -> index + 22
        else -> -1
    }
}
