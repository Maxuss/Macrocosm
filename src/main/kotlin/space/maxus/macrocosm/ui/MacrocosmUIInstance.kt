package space.maxus.macrocosm.ui

import io.papermc.paper.adventure.PaperAdventure
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.pluginManager
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftContainer
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryCustom
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.ui.animation.UIAnimation
import space.maxus.macrocosm.util.equalsAny
import java.util.concurrent.atomic.AtomicBoolean

val InventoryAction.isInUi
    get() = this.equalsAny(
        InventoryAction.PICKUP_ALL,
        InventoryAction.PICKUP_HALF,
        InventoryAction.PICKUP_SOME,
        InventoryAction.PICKUP_ONE,
        InventoryAction.MOVE_TO_OTHER_INVENTORY,
        InventoryAction.PLACE_ALL,
        InventoryAction.PLACE_ONE,
        InventoryAction.PLACE_SOME
    )

class MacrocosmUIInstance internal constructor(
    val baseUi: Inventory,
    val dimensions: UIDimensions,
    private val pages: MutableList<UIPage>,
    val holder: Player,
    val title: Component,
    val base: MacrocosmUI,
    var extraClickHandler: (UIClickData) -> Unit,
    var extraCloseHandler: (UICloseData) -> Unit,
    defaultPage: Int
) {
    private lateinit var clickHandler: Listener
    private var abandoned: Boolean = false
    private var currentPage: Int = defaultPage
    private var animationLock: AtomicBoolean = AtomicBoolean(false)

    fun reload() {
        base.render(holder.openInventory.topInventory, currentPage)
        holder.updateInventory()
    }

    fun switchPage(page: Int) {
        this.currentPage = page
        base.render(holder.openInventory.topInventory, page)
        holder.updateInventory()
    }

    fun renderAnimation(animation: UIAnimation) {
        var tick = 0
        if (animationLock.get())
            return
        animationLock.set(true)
        val pageLock = currentPage
        task(sync = false, period = 1L) {
            if (animation.shouldStop(tick) || baseUi.viewers.isEmpty() || abandoned || currentPage != pageLock) {
                it.cancel()
                animationLock.set(false)
                return@task
            }
            animation.tick(tick, baseUi, base)
            tick += 1
            holder.updateInventory()
        }
    }

    fun start() {
        clickHandler = object : Listener {
            @EventHandler
            fun onClick(e: InventoryClickEvent) {
                if (e.whoClicked !is Player || e.whoClicked.uniqueId != holder.uniqueId)
                    return
                if (e.clickedInventory == e.view.bottomInventory && (e.click.isShiftClick || e.click == ClickType.DOUBLE_CLICK)
                    || (e.clickedInventory == e.view.topInventory && (!e.action.isInUi))
                ) {
                    e.isCancelled = true
                    return
                }
                val clickData =
                    UIClickData(e, holder, (e.whoClicked as Player).macrocosm!!, baseUi, this@MacrocosmUIInstance)
                extraClickHandler(clickData)
                if (e.clickedInventory == e.view.bottomInventory)
                    return
                for (component in pages[currentPage].components.reversed()) {
                    if (component.wasClicked(e.slot, dimensions)) {
                        component.handleClick(clickData)
                        break
                    }
                }
            }

            @EventHandler
            fun onDrag(e: InventoryDragEvent) {
                if (e.whoClicked !is Player || e.whoClicked.uniqueId != holder.uniqueId)
                    return
                e.isCancelled = true
            }
        }
        pluginManager.registerEvents(clickHandler, Macrocosm)
    }

    fun switch(other: MacrocosmUI, reverse: Boolean = false): MacrocosmUIInstance {
        abandoned = true
        if (other.dimensions == this.dimensions)
            this.extraCloseHandler(UICloseData(this.holder, this.holder.macrocosm!!, this.baseUi, this))
        clickHandler.unregister()
        if (other.dimensions != dimensions) {
            // Dimensions do not fit, we need a new inventory
            val newBase = other.dimensions.bukkit(holder, other.title)
            if (!reverse)
                holder.macrocosm?.uiHistory?.add(base.id)
            else
                holder.macrocosm?.uiHistory?.removeLast()
            other.render(newBase, other.defaultPage)
            holder.closeInventory()
            holder.openInventory(newBase)
            holder.updateInventory()
            val new = other.setup(newBase, holder)
            holder.macrocosm?.openUi = new
            return new
        } else {
            // Reusing old inventory for smoother experience
            baseUi.clear()
            if (!reverse)
                holder.macrocosm?.uiHistory?.add(base.id)
            else
                holder.macrocosm?.uiHistory?.removeLast()
            val inv = holder.openInventory.topInventory as CraftInventoryCustom
            other.render(inv, other.defaultPage)
            val activeContainer = (holder.player as CraftPlayer).handle.containerMenu
            val containerId = activeContainer.containerId
            val packet = ClientboundOpenScreenPacket(
                containerId,
                CraftContainer.getNotchInventoryType(inv),
                PaperAdventure.asVanilla(other.title)
            )
            holder.macrocosm?.sendPacket(packet)
            holder.updateInventory()
            val new = other.setup(baseUi, holder)
            holder.macrocosm?.openUi = new
            return new
        }
    }

    fun close() {
        this.holder.macrocosm?.uiHistory?.clear()
        this.abandoned = true
        this.extraCloseHandler(UICloseData(this.holder, this.holder.macrocosm ?: return, this.baseUi, this))
        this.clickHandler.unregister()
    }
}
