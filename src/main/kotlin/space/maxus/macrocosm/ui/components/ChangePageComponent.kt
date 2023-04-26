package space.maxus.macrocosm.ui.components

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.UIClickData

/**
 * A component that switches to a different page.
 *
 * @param space The space this component is in.
 * @param to The page to switch to.
 * @param repr The item component representation to use.
 */
class ChangePageComponent(space: ComponentSpace, val to: Int, val repr: ItemComponentRepr) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        click.instance.switchPage(to)
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
    }

    override fun render(inv: Inventory): ItemStack = repr.item
}
