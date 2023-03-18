package space.maxus.macrocosm.ui.animation

import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.ui.MacrocosmUI

open class CompositeAnimation : UIAnimation {
    private val tasks: MutableList<RenderTask> = mutableListOf()

    override fun tick(tick: Int, inv: Inventory, ui: MacrocosmUI) {
        for (task in tasks) {
            task.tick()
        }
    }

    final override fun shouldStop(tick: Int): Boolean {
        return tasks.all { it.isComplete() }
    }

    fun track(task: RenderTask) {
        this.tasks.add(task)
    }
}
