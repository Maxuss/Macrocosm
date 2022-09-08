package space.maxus.macrocosm.graphics

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.text.text

class ItemRenderBuffer private constructor(val name: Component, val lines: List<Component>) {
    companion object {
        fun builder(name: Component) = Builder(name)
        fun stack(stack: ItemStack) = ItemRenderBuffer(stack.itemMeta.displayName() ?: text("<red>NULL ITEM"), stack.itemMeta.lore() ?: listOf())
    }

    class Builder internal constructor(val name: Component) {
        private val lines: MutableList<Component> = mutableListOf()

        fun addToBuffer(line: Component): Builder {
            this.lines.add(line)
            return this
        }

        fun addToBuffer(lines: List<Component>): Builder {
            this.lines.addAll(lines)
            return this
        }

        fun build(): ItemRenderBuffer = ItemRenderBuffer(name, lines)
    }
}
