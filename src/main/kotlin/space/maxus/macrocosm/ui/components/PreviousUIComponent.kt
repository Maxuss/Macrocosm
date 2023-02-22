package space.maxus.macrocosm.ui.components

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

class PreviousUIComponent(val space: ComponentSpace): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        val last = click.player.uiHistory.lastOrNull() ?: return
        val ui = Registry.UI.find(last)
        if(ui is MacrocosmUI.NullUi) {
            throw IllegalStateException("Can not return to delegated ui!")
        }
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
        click.instance.switch(ui, true)
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        val paper = inv.viewers.first() as? Player ?: return
        val player = paper.macrocosm ?: return
        val previousName = Registry.UI.find(player.uiHistory.lastOrNull() ?: return).title.str()
        for(slot in space.enumerate()) {
            inv.setItem(slot, ItemValue.placeholderDescripted(Material.ARROW, "<yellow>Go Back", "To $previousName"))
        }
    }
}
