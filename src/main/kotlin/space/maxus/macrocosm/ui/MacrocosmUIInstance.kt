package space.maxus.macrocosm.ui

import io.papermc.paper.adventure.PaperAdventure
import net.axay.kspigot.event.unregister
import net.axay.kspigot.extensions.pluginManager
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftContainer
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftInventoryCustom
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
import java.util.concurrent.atomic.AtomicBoolean

val InventoryAction.isInUi get() =
    this == InventoryAction.PICKUP_ALL || this == InventoryAction.PICKUP_HALF || this == InventoryAction.PICKUP_SOME || this == InventoryAction.PICKUP_ONE || this == InventoryAction.MOVE_TO_OTHER_INVENTORY

class MacrocosmUIInstance internal constructor(
    val baseUi: Inventory,
    val dimensions: UIDimensions,
    private val componentTree: MutableList<UIComponent>,
    val holder: Player,
    val title: Component,
    val base: MacrocosmUI,
    var extraClickHandler: (UIClickData) -> Unit,
) {
    private lateinit var clickHandler: Listener
    private var animationLock: AtomicBoolean = AtomicBoolean(false)

    fun reload() {
        base.render(holder.openInventory.topInventory)
        holder.updateInventory()
    }

    fun renderAnimation(animation: UIAnimation) {
        var tick = 0
        if(animationLock.get())
            return
        animationLock.set(true)
        task(sync = false, period = 1L) {
            if(animation.shouldStop(tick) || baseUi.viewers.isEmpty()) {
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
        clickHandler = object: Listener {
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
                if(e.clickedInventory == e.view.bottomInventory)
                    return
                val clickData = UIClickData(e, holder, (e.whoClicked as Player).macrocosm!!, baseUi, this@MacrocosmUIInstance)
                for (component in componentTree) {
                    if (component.wasClicked(e.slot)) {
                        component.handleClick(clickData)
                        break
                    }
                }
                extraClickHandler(clickData)
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
        clickHandler.unregister()
        if(other.dimensions != dimensions) {
            // Dimensions do not fit, we need a new inventory
            val newBase = other.dimensions.bukkit(holder, other.title)
            if(!reverse)
                holder.macrocosm?.uiHistory?.add(base.id)
            else
                holder.macrocosm?.uiHistory?.removeLast()
            other.render(newBase)
            holder.closeInventory()
            holder.openInventory(newBase)
            holder.updateInventory()
            val new = other.setup(newBase, holder)
            holder.macrocosm?.openUi = new
            return new
        } else {
            // Reusing old inventory for smoother experience
            baseUi.clear()
            if(!reverse)
                holder.macrocosm?.uiHistory?.add(base.id)
            else
                holder.macrocosm?.uiHistory?.removeLast()
            val inv = holder.openInventory.topInventory as CraftInventoryCustom
            other.render(inv)
            val activeContainer = (holder.player as CraftPlayer).handle.containerMenu
            val containerId = activeContainer.containerId
            val packet = ClientboundOpenScreenPacket(containerId, CraftContainer.getNotchInventoryType(inv), PaperAdventure.asVanilla(other.title))
            holder.macrocosm?.sendPacket(packet)
            holder.updateInventory()
            val new = other.setup(baseUi, holder)
            holder.macrocosm?.openUi = new
            return new
        }
    }

    fun close() {
        this.holder.macrocosm?.uiHistory?.clear()
        this.clickHandler.unregister()
    }
}
