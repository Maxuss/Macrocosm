package space.maxus.macrocosm.ui.components

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData

class SwitchUIComponent(
    space: ComponentSpace,
    val item: ItemComponentRepr,
    val ui: Identifier
) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        val ui = Registry.UI.find(ui)
        if (ui is MacrocosmUI.NullUi) {
            throw IllegalStateException("Can not return to delegated ui!")
        }
        click.instance.switch(ui)
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
    }

    override fun render(inv: Inventory): ItemStack = item.item
}

class DelegatedSwitchUIComponent(
    space: ComponentSpace,
    val item: ItemComponentRepr,
    val ui: MacrocosmUI
) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        click.instance.switch(ui)
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
    }

    override fun render(inv: Inventory): ItemStack = item.item
}
