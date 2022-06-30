package space.maxus.macrocosm.listeners

import net.axay.kspigot.gui.openGUI
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import space.maxus.macrocosm.item.macrocosmTag
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.recipesUsing
import space.maxus.macrocosm.util.generic.getId

object BlockClickListener : Listener {
    @EventHandler
    fun onPlaceBlock(e: BlockPlaceEvent) {
        val mc = e.itemInHand.macrocosmTag()
        if (mc.contains("BlockClicks") || e.player.inventory.itemInOffHand.macrocosmTag().contains("BlockClicks"))
            e.isCancelled = true
    }

    @EventHandler
    fun onConsume(e: PlayerItemConsumeEvent) {
        val mc = e.item.macrocosmTag()
        if (mc.contains("BlockClicks") || e.player.inventory.itemInOffHand.macrocosmTag().contains("BlockClicks"))
            e.isCancelled = true
    }

    @EventHandler
    fun openRecipeBrowser(e: PlayerInteractEvent) {
        val mc = e.item?.macrocosmTag() ?: return
        if (mc.contains("ViewRecipes")) {
            e.player.openGUI(recipesUsing(mc.getId("ViewRecipes"), e.player.macrocosm!!))
            e.isCancelled = true
        }
    }
}
