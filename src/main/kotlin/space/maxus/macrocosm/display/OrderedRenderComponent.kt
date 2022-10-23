package space.maxus.macrocosm.display

import net.kyori.adventure.text.Component

/**
 * A render component that has priority
 */
data class OrderedRenderComponent(
    /**
     * Inner component
     */
    val delegate: RenderComponent,
    /**
     * Priority of this component
     */
    val position: RenderPriority
) : RenderComponent {
    override fun title(): Component {
        return delegate.title()
    }

    override fun lines(): List<Component> {
        return delegate.lines()
    }
}
