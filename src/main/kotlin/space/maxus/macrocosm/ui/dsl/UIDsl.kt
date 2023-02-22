package space.maxus.macrocosm.ui.dsl

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.animation.UIRenderHelper
import space.maxus.macrocosm.ui.components.*

@DslMarker
annotation class UIDsl

inline fun macrocosmUi(id: String, dimensions: UIDimensions, builder: MacrocosmUIBuilder.() -> Unit): MacrocosmUI = MacrocosmUIBuilder(Identifier.parse(id), dimensions).apply(builder).build()

class MacrocosmUIBuilder(val id: Identifier, dimensions: UIDimensions) {
    @UIDsl
    var title: String = ""
    @UIDsl
    var onClick: (UIClickData) -> Unit = { }
    private val ui: MacrocosmUI = MacrocosmUI(id, dimensions, onClick)

    @UIDsl
    fun background(space: ComponentSpace = Slot.All) {
        this.ui.addComponent(PlaceholderComponent(space, UIRenderHelper.dummy(Material.GRAY_STAINED_GLASS_PANE)))
    }

    @UIDsl
    fun placeholder(space: ComponentSpace, item: ItemStack) {
        this.ui.addComponent(PlaceholderComponent(space, item))
    }

    @UIDsl
    fun button(space: ComponentSpace, display: ItemStack, handler: (UIClickData) -> Unit) {
        this.ui.addComponent(ButtonComponent(space, StaticItemRepr(display), handler))
    }

    @UIDsl
    fun button(space: ComponentSpace, icon: () -> ItemStack, handler: (UIClickData) -> Unit) {
        this.ui.addComponent(ButtonComponent(space, DynamicItemRepr(icon), handler))
    }

    @UIDsl
    fun <V> compound(space: ComponentSpace, values: Iterable<V>, icon: (V) -> ItemStack, handler: (UIClickData, V) -> Unit): SpacedCompoundComponent<V> {
        val compound = SpacedCompoundComponent(space, values.toList(), icon, handler)
        this.ui.addComponent(compound)
        return compound
    }

    @UIDsl
    fun compoundScroll(
        space: ComponentSpace,
        compound: SpacedCompoundComponent<*>,
        amount: Int = 1,
        reverse: Boolean = false,
        display: ItemStack = ItemValue.placeholderDescripted(Material.ARROW, if(!reverse) "<green>Scroll Forward" else "<red>Scroll Backward", "<blue>$amount times")
    ) {
        this.ui.addComponent(CompoundScrollComponent(space, compound, if(reverse) -amount else amount, display))
    }

    @UIDsl
    fun compoundWidthScroll(
        space: ComponentSpace,
        compound: SpacedCompoundComponent<*>,
        reverse: Boolean = false,
        display: ItemStack = ItemValue.placeholder(Material.ARROW, if(!reverse) "<green>Scroll Forward" else "<red>Scroll Backward")
    ) {
        this.ui.addComponent(CompoundWidthScrollComponent(space, compound, display, reverse))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        ui: String,
        display: ItemStack,
    ) {
        this.ui.addComponent(SwitchUIComponent(space, StaticItemRepr(display), Identifier.parse(ui)))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        ui: String,
        icon: () -> ItemStack,
    ) {
        this.ui.addComponent(SwitchUIComponent(space, DynamicItemRepr(icon), Identifier.parse(ui)))
    }

    @UIDsl
    fun goBack(
        space: ComponentSpace,
    ) {
        this.ui.addComponent(PreviousUIComponent(space))
    }

    fun build(): MacrocosmUI {
        ui.title = text(title)
        return ui
    }

    infix fun Slot.rect(other: Slot): ComponentSpace {
        val (min, max) = listOf(this, other).let { list -> list.minBy { it.value } to list.maxBy { it.value } }
        return RectComponentSpace(min, max)
    }

    infix fun Slot.lin(other: Slot): ComponentSpace {
        return LinearComponentSpace((this.value..other.value).toList())
    }
}
