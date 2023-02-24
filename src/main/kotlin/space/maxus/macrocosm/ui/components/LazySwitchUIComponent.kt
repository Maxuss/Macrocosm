package space.maxus.macrocosm.ui.components

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

class LazySwitchUIComponent(val space: ComponentSpace, val item: ItemComponentRepr, val ui: () -> MacrocosmUI): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        click.instance.switch(ui())
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        val item = item.item
        for(slot in space.enumerate()) {
            inv.setItem(slot, item)
        }
    }
}
