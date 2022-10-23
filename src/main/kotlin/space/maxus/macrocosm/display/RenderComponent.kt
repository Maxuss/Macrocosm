package space.maxus.macrocosm.display

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.text.text

/**
 * A component which can be rendered in a sidebar
 */
interface RenderComponent {
    /**
     * Returns the title of this component
     */
    fun title(): Component

    /**
     * Returns all the lines inside this component
     */
    fun lines(): List<Component>

    companion object {
        /**
         * Returns a simple (static, unformatted string) render component
         */
        fun simple(title: String, description: String): RenderComponent = Simple(title, description)

        /**
         * Returns a fixed (static, formatted chat component) render component
         */
        fun fixed(title: Component, lines: List<Component>): RenderComponent = Fixed(title, lines)

        /**
         * Returns a dynamic (changing, formatted chat comonent) render component
         */
        fun dynamic(title: () -> Component, lines: () -> List<Component>): RenderComponent = Dynamic(title, lines)
    }

    private data class Dynamic(val title: () -> Component, val lines: () -> List<Component>) : RenderComponent {
        override fun title(): Component {
            return this.title.invoke()
        }

        override fun lines(): List<Component> {
            return this.lines.invoke()
        }
    }

    private data class Fixed(val title: Component, val lines: List<Component>) : RenderComponent {
        override fun title(): Component {
            return title
        }

        override fun lines(): List<Component> {
            return lines
        }
    }

    private class Simple(val title: String, description: String) : RenderComponent {
        private val desc: List<Component>

        init {
            val reduced = description.reduceToList(15).map { text("<gray>$it").noitalic() }.toMutableList()
            reduced.removeIf { it.toLegacyString().isBlank() }
            desc = reduced
        }

        override fun title(): Component {
            return text(title)
        }

        override fun lines(): List<Component> {
            return desc
        }
    }

}
