package space.maxus.macrocosm.recipes

import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.util.Identifier

interface Ingredient {
    fun stack(): ItemStack
    fun item(): MacrocosmItem
    fun id(): Identifier
}
