package space.maxus.macrocosm.ui

import net.dv8tion.jda.annotations.DeprecatedSince
import net.dv8tion.jda.annotations.ForRemoval
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.Ensure

open class MacrocosmUI(
    val id: Identifier,
    val dimensions: UIDimensions,
    val extraClickHandler: (UIClickData) -> Unit = { },
) {
    var title: Component = Component.empty()
    var defaultPage: Int = 0
    private val pages: MutableList<UIPage> = mutableListOf()

    fun addPage(page: UIPage): MacrocosmUI {

        this.pages.add(page)
        return this
    }

    @Deprecated("`addPage` with page logic should be used instead")
    @DeprecatedSince("0.4.1")
    @ForRemoval(deadline = "0.5.0")
    fun addComponent(component: UIComponent): MacrocosmUI {
        if(this.pages.isEmpty())
            this.pages.add(UIPage(0))
        this.pages.last().components.add(component)
        return this
    }

    fun withTitle(title: Component): MacrocosmUI {
        this.title = title
        return this
    }

    fun open(to: Player): MacrocosmUIInstance {
        val base = dimensions.bukkit(to, title)
        render(base, defaultPage)
        to.openInventory(base)
        val instance = setup(base, to)
        to.macrocosm?.openUi = instance
        return instance
    }

    fun render(inside: Inventory, page: Int) {
        Ensure.isEqual(inside.size, dimensions.size, "Invalid UI to render inside")

        inside.clear()

        for(component in pages[page].components) {
            component.render(inside, this)
        }
    }

    fun setup(base: Inventory, holder: Player): MacrocosmUIInstance {
        return MacrocosmUIInstance(base, dimensions, pages.map { val copy = UIPage(it.index); copy.components = it.components.toMutableList().asReversed(); copy }.toMutableList(), holder, title, this, extraClickHandler, defaultPage).apply(MacrocosmUIInstance::start)
    }

    object NullUi: MacrocosmUI(Identifier.NULL, UIDimensions.SIX_X_NINE)
}
