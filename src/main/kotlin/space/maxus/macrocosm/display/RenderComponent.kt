package space.maxus.macrocosm.display

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.text.comp

interface RenderComponent {
    fun title(): Component
    fun lines(): List<Component>

    companion object {
        fun simple(title: String, description: String): RenderComponent = Simple(title, description)
        fun fixed(title: Component, lines: List<Component>): RenderComponent = Fixed(title, lines)
        fun dynamic(title: () -> Component, lines: () -> List<Component>): RenderComponent = Dynamic(title, lines)
    }

    private data class Dynamic(val title: () -> Component, val lines: () -> List<Component>): RenderComponent {
        override fun title(): Component {
            return this.title.invoke()
        }

        override fun lines(): List<Component> {
            return this.lines.invoke()
        }
    }

    private data class Fixed(val title: Component, val lines: List<Component>): RenderComponent {
        override fun title(): Component {
            return title
        }

        override fun lines(): List<Component> {
            return lines
        }
    }

    private class Simple(val title: String, description: String): RenderComponent {
        private val desc: List<Component>

        init {
            val reduced = description.reduceToList(15).map { comp("<gray>$it").noitalic() }.toMutableList()
            reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
            desc = reduced
        }

        override fun title(): Component {
            return comp(title)
        }

        override fun lines(): List<Component> {
            return desc
        }
    }

}
