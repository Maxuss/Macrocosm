package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.ItemStack

interface ItemComponentRepr {
    val item: ItemStack
}

data class StaticItemRepr(override val item: ItemStack) : ItemComponentRepr

data class DynamicItemRepr(private val getter: () -> ItemStack) : ItemComponentRepr {
    override val item: ItemStack get() = getter()
}

val ItemStack.repr: ItemComponentRepr get() = StaticItemRepr(this)
