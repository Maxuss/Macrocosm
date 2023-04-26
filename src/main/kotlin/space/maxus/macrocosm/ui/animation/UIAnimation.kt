package space.maxus.macrocosm.ui.animation

import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.ui.MacrocosmUI

/**
 * UI Animation is a simple interface that defines a tick-based animation.
 */
interface UIAnimation {
    /**
     * Called every tick of the animation.
     */
    fun tick(tick: Int, inv: Inventory, ui: MacrocosmUI)

    /**
     * Called to check if the animation should stop.
     */
    fun shouldStop(tick: Int): Boolean
}
