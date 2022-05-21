package space.maxus.macrocosm.recipes.types

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.recipes.RecipeContext
import space.maxus.macrocosm.recipes.MacrocosmRecipe
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.id

fun shapelessRecipe(id: String, result: MacrocosmItem, vararg ingredients: Pair<Identifier, Int>): MacrocosmRecipe =
    ShapelessRecipe(id(id), ingredients.toList(), result)

class ShapelessRecipe(
    override val id: Identifier,
    private val ingredients: List<Pair<Identifier, Int>>,
    private val result: MacrocosmItem
) : MacrocosmRecipe {
    @Suppress("KotlinConstantConditions")
    override fun matches(
        player: MacrocosmPlayer,
        inv: Inventory,
        modify: Boolean
    ): Pair<Boolean, HashMap<Int, Pair<ItemStack, Int>>> {
        val cache = hashMapOf<Int, Pair<ItemStack, Int>>()
        for (x in 0..2) {
            for (y in 0..2) {
                val index = gridIndices[x][y]
                val item = inv.getItem(index) ?: continue
                if (item.type.isAir)
                    continue
                val id = item.macrocosm!!.id
                val amount = item.amount
                for ((expectedId, expectedAmount) in ingredients) {
                    if (expectedId == id && amount >= expectedAmount) {
                        cache[index] = Pair(item, expectedAmount)
                    } else {
                        return Pair(false, hashMapOf())
                    }
                }
            }
        }
        if (cache.size != ingredients.size)
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

    override fun ingredients(): List<List<Pair<Identifier, Int>>> {
        return listOf(ingredients)
    }

    private val gridIndices = listOf(
        listOf(10, 11, 12),
        listOf(19, 20, 21),
        listOf(28, 29, 30)
    )
}
