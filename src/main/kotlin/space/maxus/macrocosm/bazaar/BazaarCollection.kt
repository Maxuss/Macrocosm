package space.maxus.macrocosm.bazaar

import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.general.varargs

enum class BazaarCollection(val displayName: String, val description: String, val displayItem: ItemStack, vararg items: String) {

    ;

    val items by varargs(items) { id(it) }
}
