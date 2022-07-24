package space.maxus.macrocosm.display

import net.kyori.adventure.text.Component

data class OrderedRenderComponent(val delegate: RenderComponent, val position: RenderPriority) : RenderComponent {
    override fun title(): Component {
        return delegate.title()
    }

    override fun lines(): List<Component> {
        return delegate.lines()
    }
}
