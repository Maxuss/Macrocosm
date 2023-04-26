package space.maxus.macrocosm.ui.components

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.ui.UIClickData

/**
 * A component that closes the inventory it is in.
 * @param space The space this component is in.
 * @param item The item to display.
 */
class CloseUIComponent(
    space: ComponentSpace,
    val item: ItemStack = ItemValue.placeholder(Material.BARRIER, "<red>Close")
) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.paper.closeInventory()
        sound(Sound.UI_BUTTON_CLICK) {
            volume = .5f
            playFor(click.paper)
        }
    }

    override fun render(inv: Inventory): ItemStack = item
}
