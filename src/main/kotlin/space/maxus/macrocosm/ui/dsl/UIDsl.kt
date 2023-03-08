package space.maxus.macrocosm.ui.dsl

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.*
import space.maxus.macrocosm.ui.animation.CompositeAnimation
import space.maxus.macrocosm.ui.animation.RenderTask
import space.maxus.macrocosm.ui.animation.UIRenderHelper
import space.maxus.macrocosm.ui.components.*
import space.maxus.macrocosm.util.identity

@DslMarker
annotation class UIDsl

@UIDsl
inline fun macrocosmUi(id: String, dimensions: UIDimensions, builder: MacrocosmUIBuilder.() -> Unit): MacrocosmUI = MacrocosmUIBuilder(Identifier.parse(id), dimensions).apply(builder).build()

class MacrocosmUIBuilder(val id: Identifier, dimensions: UIDimensions) {
    @UIDsl
    var title: String = ""

    private val ui: MacrocosmUI = MacrocosmUI(id, dimensions)
    @UIDsl
    var onClick: (UIClickData) -> Unit
        set(v) { ui.extraClickHandler = v }
        get() = ui.extraClickHandler
    @UIDsl
    var onClose: (UICloseData) -> Unit
        set(v) { ui.extraCloseHandler = v }
        get() = ui.extraCloseHandler

    private var pageIndex: Int = 0

    @UIDsl
    fun page(idx: Int = pageIndex + 1, builder: PageBuilder.() -> Unit) {
        val page = UIPage(idx)
        this.pageIndex = idx
        val bld = PageBuilder(page)
        bld.apply(builder)
        this.ui.addPage(bld.page)
    }

    @UIDsl
    fun pageLazy(idx: Int = pageIndex + 1, builder: PageBuilder.() -> Unit) {
        this.pageIndex = idx
        val page = LazyUIPage(idx, builder)
        this.ui.addPage(page)
    }

    fun build(): MacrocosmUI {
        ui.title = text(title)
        return ui
    }
}

class LazyUIPage(idx: Int, private val config: PageBuilder.() -> Unit): UIPage(idx) {
    override var components: MutableList<UIComponent>
        get() = PageBuilder(UIPage(index)).apply(config).page.components
        set(_) { /* no-op */ }
}

class PageBuilder(internal val page: UIPage) {
    @UIDsl
    fun background(space: ComponentSpace = Slot.All) {
        this.page.addComponent(PlaceholderComponent(space, UIRenderHelper.dummy(Material.GRAY_STAINED_GLASS_PANE)))
    }

    @UIDsl
    fun placeholder(space: ComponentSpace, item: ItemStack) {
        this.page.addComponent(PlaceholderComponent(space, item))
    }

    @UIDsl
    fun button(space: ComponentSpace, display: ItemStack, handler: (UIClickData) -> Unit) {
        this.page.addComponent(ButtonComponent(space, StaticItemRepr(display), handler))
    }

    @UIDsl
    fun button(space: ComponentSpace, icon: () -> ItemStack, handler: (UIClickData) -> Unit) {
        this.page.addComponent(ButtonComponent(space, DynamicItemRepr(icon), handler))
    }

    @UIDsl
    fun <V> compound(space: ComponentSpace, values: Iterable<V>, icon: (V) -> ItemStack, handler: (UIClickData, V) -> Unit): CompoundComponent<V> {
        val compound = CompoundComponent(space, values.toList(), icon, handler)
        this.page.addComponent(compound)
        return compound
    }

    @UIDsl
    fun <V> compound(space: ComponentSpace, values: () -> List<V>, icon: (V) -> ItemStack, handler: (UIClickData, V) -> Unit): CompoundComponent<V> {
        val compound = LazyCompoundComponent(space, values, icon, handler)
        this.page.addComponent(compound)
        return compound
    }

    @UIDsl
    fun <V> transparentCompound(space: ComponentSpace, values: () -> List<V>, icon: (V) -> ItemStack, handler: (UIClickData, V) -> Unit): CompoundComponent<V> {
        val compound = LazyCompoundComponent(space, values, icon, handler, transparent = true)
        this.page.addComponent(compound)
        return compound
    }

    @UIDsl
    fun <V> transparentCompound(space: ComponentSpace, values: Iterable<V>, icon: (V) -> ItemStack, handler: (UIClickData, V) -> Unit): CompoundComponent<V> {
        val compound = CompoundComponent(space, values.toList(), icon, handler, transparent = true)
        this.page.addComponent(compound)
        return compound
    }

    @UIDsl
    fun storageSlot(space: ComponentSpace, fits: (ItemStack) -> Boolean = { true }, onPut: (UIClickData, ItemStack) -> Unit = { _, _ -> }, onTake: (UIClickData, ItemStack) -> Unit = { _, _ -> }) {
        this.page.addComponent(StorageComponent(space, fits, onPut, onTake))
    }

    @UIDsl
    fun compoundScroll(
        space: ComponentSpace,
        compound: CompoundComponent<*>,
        amount: Int = 1,
        reverse: Boolean = false,
        display: ItemStack = ItemValue.placeholderDescripted(Material.ARROW, if(!reverse) "<green>Scroll Forward" else "<red>Scroll Backward", "<blue>$amount times")
    ) {
        this.page.addComponent(CompoundScrollComponent(space, compound, if(reverse) -amount else amount, display))
    }

    @UIDsl
    fun compoundWidthScroll(
        space: ComponentSpace,
        compound: CompoundComponent<*>,
        reverse: Boolean = false,
        display: ItemStack = ItemValue.placeholder(Material.ARROW, if(!reverse) "<green>Scroll Forward" else "<red>Scroll Backward")
    ) {
        this.page.addComponent(CompoundWidthScrollComponent(space, compound, display, reverse))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        ui: String,
        display: ItemStack,
    ) {
        this.page.addComponent(SwitchUIComponent(space, StaticItemRepr(display), Identifier.parse(ui)))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        ui: String,
        icon: () -> ItemStack,
    ) {
        this.page.addComponent(SwitchUIComponent(space, DynamicItemRepr(icon), Identifier.parse(ui)))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        ui: MacrocosmUI,
        display: ItemStack,
    ) {
        this.page.addComponent(DelegatedSwitchUIComponent(space, StaticItemRepr(display), ui))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        lazy: () -> MacrocosmUI,
        display: ItemStack,
    ) {
        this.page.addComponent(LazySwitchUIComponent(space, StaticItemRepr(display), lazy))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        ui: MacrocosmUI,
        icon: () -> ItemStack,
    ) {
        this.page.addComponent(DelegatedSwitchUIComponent(space, DynamicItemRepr(icon), ui))
    }

    @UIDsl
    fun switchUi(
        space: ComponentSpace,
        lazy: () -> MacrocosmUI,
        icon: () -> ItemStack,
    ) {
        this.page.addComponent(LazySwitchUIComponent(space, DynamicItemRepr(icon), lazy))
    }


    @UIDsl
    fun goBack(
        space: ComponentSpace,
    ) {
        this.page.addComponent(PreviousUIComponent(space))
    }

    @UIDsl
    fun goBack(
        space: ComponentSpace,
        delegated: MacrocosmUI
    ) {
        this.page.addComponent(DelegatedSwitchUIComponent(space, StaticItemRepr(ItemValue.placeholderDescripted(Material.ARROW, "<yellow>Go Back", "To ${delegated.title.str()}")), delegated))
    }

    @UIDsl
    fun goBack(
        space: ComponentSpace,
        lazy: () -> MacrocosmUI,
        title: String = lazy().title.str()
    ) {
        this.page.addComponent(LazySwitchUIComponent(space, StaticItemRepr(ItemValue.placeholderDescripted(Material.ARROW, "<yellow>Go Back", "To $title")), lazy))
    }

    @UIDsl
    fun changePage(
        space: ComponentSpace,
        to: Int
    ) {
        val item = if(this.page.index > to) ItemValue.placeholderDescripted(Material.ARROW, "<green>Next Page") else ItemValue.placeholderDescripted(Material.ARROW, "<red>Previous Page")
        this.page.addComponent(ChangePageComponent(space, to, StaticItemRepr(item)))
    }

    @UIDsl
    fun changePage(
        space: ComponentSpace,
        to: Int,
        item: ItemStack
    ) {
        this.page.addComponent(ChangePageComponent(space, to, StaticItemRepr(item)))
    }

    @UIDsl
    fun changePage(
        space: ComponentSpace,
        to: Int,
        item: () -> ItemStack
    ) {
        this.page.addComponent(ChangePageComponent(space, to, DynamicItemRepr(item)))
    }

    @UIDsl
    inline fun UIClickData.animate(handler: AnimationBuilder.() -> Unit) {
        val builder = AnimationBuilder(this.inventory)
        builder.apply(handler)
        this.instance.renderAnimation(builder.animation)
    }

    @UIDsl
    fun close(space: ComponentSpace = Slot.RowSixSlotFive, item: ItemStack = ItemValue.placeholder(Material.BARRIER, "<red>Close")) {
        this.page.addComponent(CloseUIComponent(space, item))
    }

    infix fun Slot.rect(other: Slot): ComponentSpace {
        val (min, max) = listOf(this, other).let { list -> list.minBy { it.value } to list.maxBy { it.value } }
        return RectComponentSpace(min, max)
    }

    infix fun Slot.lin(other: Slot): ComponentSpace {
        return LinearComponentSpace((this.value..other.value).toList())
    }
}

class AnimationBuilder(private val inv: Inventory) {
    var animation = CompositeAnimation()

    @UIDsl
    fun instant(space: ComponentSpace, item: ItemStack) {
        UIRenderHelper.instant(inv, item, space)
    }

    @UIDsl
    fun draw(space: ComponentSpace, item: ItemStack, frequency: Int = 1, perTick: Int = 1, preconfig: (RenderTask) -> RenderTask = identity()) {
        animation.track(preconfig(UIRenderHelper.draw(inv, item, space, perTick, frequency)))
    }

    @UIDsl
    fun dissolve(space: ComponentSpace, to: ItemStack, trail: ItemStack, frequency: Int = 1, delay: Int = frequency, perTick: Int = 1, preconfig: (RenderTask) -> RenderTask = identity()) {
        animation.track(preconfig(UIRenderHelper.drawDissolve(inv, to, trail, space, perTick, frequency, delay)))
    }

    @UIDsl
    fun dissolveInstant(space: ComponentSpace, to: ItemStack, trail: ItemStack, frequency: Int = 1, perTick: Int = 1, preconfig: (RenderTask) -> RenderTask = identity()) {
        animation.track(preconfig(UIRenderHelper.instantDissolve(inv, to, trail, space, perTick, frequency)))
    }

    @UIDsl
    fun burn(space: ComponentSpace, base: ItemStack, edge: ItemStack, trail: ItemStack, frequency: Int = 1, delay: Int = frequency + 1, preconfig: (RenderTask) -> RenderTask = identity()) {
        animation.track(preconfig(UIRenderHelper.burn(inv, base, edge, trail, space, frequency, delay)))
    }

    fun dummy(mat: Material) = UIRenderHelper.dummy(mat)
}
