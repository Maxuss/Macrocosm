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
import space.maxus.macrocosm.util.id

fun shapedRecipe(
    id: String,
    result: MacrocosmItem,
    amount: Int,
    matrix: List<String>,
    vararg ingredients: Pair<Char, Pair<Identifier, Int>>
): MacrocosmRecipe = ShapedRecipe(id(id), matrix, ingredients.toMap(), result, amount)

class ShapedRecipe(
    override val id: Identifier,
    pattern: List<String>,
    private val ingredientMap: Map<Char, Pair<Identifier, Int>>,
    private val result: MacrocosmItem,
    private val amount: Int = 1
) : MacrocosmRecipe {
    private val ingredients: List<List<Pair<Identifier, Int>>>

    init {
        val newPattern = pattern.toMutableList()
        while (newPattern.size < 2)
            newPattern.add("   ")
        ingredients = newPattern.map {
            it.toCharArray().map { c ->
                if (c == ' ') Pair(Identifier.NULL, 0) else ingredientMap[c]
                    ?: throw IllegalStateException("Character '$c' was not defined in the ingredient map!")
            }
        }
        // example
        // [ [ enchanted_diamond, 1 ]  [ enchanted_diamond, 1 ] [ null, 0 ]  ]
        // [ [ stick, 1             ]  [ enchanted_diamond, 1 ] [ null, 0 ]  ]
        // [ [ stick, 1             ]  [ null,              0 ] [ null, 0 ]  ]
    }

    @Suppress("KotlinConstantConditions")
    override fun matches(
        player: MacrocosmPlayer,
        inv: Inventory,
        modify: Boolean
    ): Pair<Boolean, HashMap<Int, Pair<ItemStack, Int>>> {
        // { item index, (item to be modified, amount to remove) }
        val cachedIndices: HashMap<Int, Pair<ItemStack, Int>> = hashMapOf()

        // iterating through Xs and Ys
        for (x in 0..2) {
            for (y in 0..2) {
                // example
                // x: 0 y: 1 -> row 1, column 2
                // on matrix:
                // [ 0 1 0 ]
                // [ 0 0 0 ]
                // [ 0 0 0 ]
                val (expectedId, expectedAmount) = ingredients[x][y]
                val itemIndex = gridIndices[x][y]
                val item = inv.getItem(itemIndex) ?: if (expectedId == Identifier.NULL) continue else return Pair(
                    false,
                    hashMapOf()
                )
                if (item.type.isAir && expectedId == Identifier.NULL) continue
                val mcItem =
                    item.macrocosm!! // asserting that the item is not null, because we checked that it is not air before
                val (itemId, itemAmount) = Pair(mcItem.id, item.amount)

                if (expectedId == Identifier.NULL) {
                    // if we defined an empty space, ensure that the item on this position is null
                    // (actually we have checked it already, but doing that just in case)
                    if (itemId != Identifier("minecraft", "air"))
                        return Pair(false, hashMapOf())
                    continue
                }

                // ensure that item's id is the one we specified, and it has the amount we need
                if (itemId == expectedId && itemAmount >= expectedAmount) {
                    // checks passed, we can cache the amount of items now
                    cachedIndices[itemIndex] = Pair(item, expectedAmount)
                    continue
                }
                return Pair(false, hashMapOf())
            }
        }

        if (modify) {
            // all checks complete, and now we can actually reduce the amount of items
            for ((index, pair) in cachedIndices) {
                val (item, amount) = pair
                item.amount -= amount
                if (item.amount <= 0)
                    inv.setItem(index, null)
                else inv.setItem(index, item)
            }
        }

        return Pair(true, cachedIndices)
    }

    companion object {
        private val gridIndices = listOf(
            listOf(10, 11, 12),
            listOf(19, 20, 21),
            listOf(28, 29, 30)
        )
    }

    override fun assemble(ctx: RecipeContext, player: MacrocosmPlayer): ItemStack {
        val important = ctx.mostImportantItem() ?: return result.clone().build() ?: ItemStack(Material.AIR)
        val cloned = result.clone()
        important.transfer(cloned)
        val built = cloned.build() ?: ItemStack(Material.AIR)
        built.amount = amount
        return built
    }

    override fun resultItem(): ItemStack {
        val built = result.build() ?: ItemStack(Material.AIR)
        built.amount = amount
        return built
    }

    override fun ingredients(): List<List<Pair<Identifier, Int>>> {
        return ingredients
    }
}
