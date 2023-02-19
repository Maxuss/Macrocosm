package space.maxus.macrocosm.ui

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.Ensure

open class MacrocosmUI(
    val id: Identifier,
    val dimensions: UIDimensions,
    val extraClickHandler: (UIClickData) -> Unit = { },
) {
    var title: Component = Component.empty()
    var componentTree: MutableList<UIComponent> = mutableListOf()

    fun addComponent(component: UIComponent): MacrocosmUI {
        this.componentTree.add(component)
        return this
    }

    fun withTitle(title: Component): MacrocosmUI {
        this.title = title
        return this
    }

    fun open(to: Player): MacrocosmUIInstance {
        val base = dimensions.bukkit(to, title)
        render(base)
        to.openInventory(base)
        val instance = setup(base, to)
        to.macrocosm?.openUi = instance
        return instance
    }

    fun render(inside: Inventory) {
        Ensure.isEqual(inside.size, dimensions.size, "Invalid UI to render inside")

        inside.clear()

        for(component in componentTree) {
            component.render(inside, this)
        }
    }

    fun setup(base: Inventory, holder: Player): MacrocosmUIInstance {
        return MacrocosmUIInstance(base, dimensions, componentTree.toMutableList().asReversed(), holder, title, this, extraClickHandler).apply(MacrocosmUIInstance::start)
    }
}
