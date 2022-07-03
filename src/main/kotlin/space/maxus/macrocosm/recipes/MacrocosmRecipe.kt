package space.maxus.macrocosm.recipes

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier

interface MacrocosmRecipe {
    val id: Identifier
    fun matches(
        player: MacrocosmPlayer,
        inv: Inventory,
        grid: List<ItemStack?>,
        modify: Boolean
    ): Pair<Boolean, HashMap<Int, Pair<ItemStack, Int>>>

    fun assemble(ctx: RecipeContext, player: MacrocosmPlayer): ItemStack
    fun resultItem(): ItemStack
    fun resultMacrocosm(): MacrocosmItem
    fun ingredients(): List<List<Pair<Identifier, Int>>>
}
