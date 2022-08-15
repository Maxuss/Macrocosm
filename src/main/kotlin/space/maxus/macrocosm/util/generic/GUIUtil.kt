package space.maxus.macrocosm.util.generic

import net.axay.kspigot.gui.*
import org.bukkit.inventory.ItemStack

class MutableButton<T: ForInventory>(var currentDisplay: ItemStack, val handler: (MutableButton<T>, GUIClickEvent<T>) -> ItemStack): GUIElement<T>() {
    override fun getItemStack(slot: Int): ItemStack {
        return currentDisplay
    }

    override fun onClickElement(clickEvent: GUIClickEvent<T>) {
        handler(this, clickEvent)
    }
}

inline fun <reified T: ForInventory> GUIPageBuilder<T>.mutableButton(slot: SingleInventorySlot<out T>, icon: ItemStack, noinline onClick: (MutableButton<T>,GUIClickEvent<T>) -> ItemStack) {
    val btn = MutableButton(icon) { self, e ->
        onClick(self, e)
    }

    val defineSlotsMtd = this.javaClass.declaredMethods.firstOrNull { mtd -> mtd.name == "defineSlots" } ?: return
    if(!defineSlotsMtd.canAccess(this))
        defineSlotsMtd.isAccessible = true
    defineSlotsMtd.invoke(this, slot, btn)
}

inline fun <reified T: ForInventory> GUIPageBuilder<T>.itemSlot(slot: SingleInventorySlot<out T>, empty: ItemStack, noinline onClick: (MutableButton<T>,GUIClickEvent<T>) -> ItemStack) {
    val button = MutableButton(empty) { self, e ->
        e.bukkitEvent.isCancelled = true
        val curs = e.bukkitEvent.cursor
        if(curs == null) {
            e.player.setItemOnCursor(self.currentDisplay)
            empty
        } else if(self.currentDisplay != empty) {
            if(curs.isSimilar(self.currentDisplay)) {
                curs.amount += self.currentDisplay.amount
                e.player.setItemOnCursor(curs)
                empty
            } else {
                e.player.setItemOnCursor(self.currentDisplay)
                onClick(self, e)
            }
        } else {
            onClick(self, e)
        }
    }

    val defineSlotsMtd = this.javaClass.declaredMethods.firstOrNull { mtd -> mtd.name == "defineSlots" } ?: return
    if(!defineSlotsMtd.canAccess(this))
        defineSlotsMtd.isAccessible = true
    defineSlotsMtd.invoke(this, slot, button)
}
