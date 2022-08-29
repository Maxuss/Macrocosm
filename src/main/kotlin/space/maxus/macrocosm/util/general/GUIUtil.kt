package space.maxus.macrocosm.util.general

import net.axay.kspigot.gui.*
import org.bukkit.inventory.ItemStack

class MutableButton<T : ForInventory>(
    var currentDisplay: ItemStack,
    val handler: (MutableButton<T>, GUIClickEvent<T>) -> ItemStack
) : GUIElement<T>() {
    override fun getItemStack(slot: Int): ItemStack {
        return currentDisplay
    }

    override fun onClickElement(clickEvent: GUIClickEvent<T>) {
        handler(this, clickEvent)
    }
}

inline fun <reified T : ForInventory> GUIPageBuilder<T>.mutableButton(
    slot: SingleInventorySlot<out T>,
    icon: ItemStack,
    noinline onClick: (MutableButton<T>, GUIClickEvent<T>) -> ItemStack
) {
    val btn = MutableButton(icon) { self, e ->
        onClick(self, e)
    }

    val defineSlotsMtd = this.javaClass.declaredMethods.firstOrNull { mtd -> mtd.name == "defineSlots" } ?: return
    if (!defineSlotsMtd.canAccess(this))
        defineSlotsMtd.isAccessible = true
    defineSlotsMtd.invoke(this, slot, btn)
}
