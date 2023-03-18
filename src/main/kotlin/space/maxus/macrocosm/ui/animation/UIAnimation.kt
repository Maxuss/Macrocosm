package space.maxus.macrocosm.ui.animation

import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.ui.MacrocosmUI

interface UIAnimation {
    fun tick(tick: Int, inv: Inventory, ui: MacrocosmUI)
    fun shouldStop(tick: Int): Boolean
}
