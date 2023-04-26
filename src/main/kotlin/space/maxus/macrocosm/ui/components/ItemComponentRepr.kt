package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.ItemStack

/**
 * A representation of an item that can be placed in a UI
 */
interface ItemComponentRepr {
    /**
     * The item to be displayed
     */
    val item: ItemStack
}

/**
 * A representation of an item that is static, i.e. it doesn't change
 */
data class StaticItemRepr(override val item: ItemStack) : ItemComponentRepr

/**
 * A representation of an item that is dynamic and is calculated every time it is rendered
 *
 * @param getter The function that returns the item to be displayed
 */
data class DynamicItemRepr(private val getter: () -> ItemStack) : ItemComponentRepr {
    override val item: ItemStack get() = getter()
}

/**
 * Converts an itemstack to a static item component representation
 */
val ItemStack.repr: ItemComponentRepr get() = StaticItemRepr(this)
