package space.maxus.macrocosm.util.data

import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.util.annotations.UnsafeFeature

@UnsafeFeature
class Unsafe(private val internalId: Int) {
    fun reloadItem(item: ItemStack, player: MacrocosmPlayer): ItemStack? {
        return item.macrocosm?.build(player)
    }
}
