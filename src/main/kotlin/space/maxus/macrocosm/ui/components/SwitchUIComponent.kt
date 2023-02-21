package space.maxus.macrocosm.ui.components

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

data class SwitchUIComponent(
    val space: ComponentSpace,
    val item: ItemComponentRepr,
    val ui: Identifier
): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        click.instance.switch(Registry.UI.find(ui))
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        val stack = item.item
        for(slot in space.enumerate()) {
            inv.setItem(slot, stack)
        }
    }
}
