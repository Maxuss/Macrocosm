package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.ui.*

data class ReactiveComponent<V, B: UIComponent>(
    val base: B,
    val react: (B, V, MacrocosmUIInstance) -> Unit
): UIComponent {
    override fun handleClick(click: UIClickData) {
        base.handleClick(click)
    }

    override fun wasClicked(slot: Int, dim: UIDimensions): Boolean {
        return base.wasClicked(slot, dim)
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        base.render(inv, ui)
    }

    fun send(value: V, ui: MacrocosmUIInstance) {
        this.react(base, value, ui)
        ui.reload()
    }
}
