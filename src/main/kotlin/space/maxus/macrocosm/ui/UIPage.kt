package space.maxus.macrocosm.ui

open class UIPage(val index: Int) {
    open var components: MutableList<UIComponent> = mutableListOf()

    fun addComponent(component: UIComponent) {
        this.components.add(component)
    }
}
