package space.maxus.macrocosm.ui

data class UIPage(val index: Int) {
    var components: MutableList<UIComponent> = mutableListOf()

    fun addComponent(component: UIComponent) {
        this.components.add(component)
    }
}
