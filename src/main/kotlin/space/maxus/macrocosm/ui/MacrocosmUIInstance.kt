package space.maxus.macrocosm.ui

import io.papermc.paper.adventure.PaperAdventure
import net.axay.kspigot.event.listen
import net.axay.kspigot.event.unregister
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftContainer
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftInventoryCustom
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.players.macrocosm

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

    fun reload() {
        base.render(holder.openInventory.topInventory)
        holder.updateInventory()
    }

    fun start() {
        clickHandler = listen<InventoryClickEvent> { e ->
            if(e.whoClicked !is Player || e.whoClicked.uniqueId != holder.uniqueId)
                return@listen
            if(e.clickedInventory == e.view.bottomInventory && (e.click.isShiftClick || e.click == ClickType.DOUBLE_CLICK)) {
                e.isCancelled = true
                return@listen
            }
            val clickData = UIClickData(e, holder, (e.whoClicked as Player).macrocosm!!, baseUi, this)
            for(component in componentTree) {
                if(component.wasClicked(e.slot)) {
                    component.handleClick(clickData)
                    break
                }
            }
            extraClickHandler(clickData)
        }
    }

    fun switch(other: MacrocosmUI): MacrocosmUIInstance {
        clickHandler.unregister()
        if(other.dimensions != dimensions) {
            // Dimensions do not fit, we need a new inventory
            val newBase = other.dimensions.bukkit(holder, other.title)
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
        this.clickHandler.unregister()
    }
}
