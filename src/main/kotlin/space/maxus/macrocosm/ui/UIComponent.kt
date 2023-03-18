package space.maxus.macrocosm.ui

import org.bukkit.inventory.Inventory

/**
 * An interface for tree-based components
 */
interface UIComponent {
    fun handleClick(click: UIClickData)
    fun render(inv: Inventory, ui: MacrocosmUI) {
        throw IllegalStateException("Override the `render` method in your UI component!")
    }

    fun wasClicked(slot: Int, dim: UIDimensions): Boolean
}
