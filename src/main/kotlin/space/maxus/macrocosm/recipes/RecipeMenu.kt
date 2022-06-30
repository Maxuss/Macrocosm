package space.maxus.macrocosm.recipes

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.server
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.annotations.DevOnly
import space.maxus.macrocosm.util.anyNull
import space.maxus.macrocosm.util.containsAny
import space.maxus.macrocosm.util.generic.Debug
import space.maxus.macrocosm.util.generic.collect
import space.maxus.macrocosm.util.giveOrDrop

@Suppress("SENSELESS_COMPARISON")
object RecipeMenu : Listener {
    fun craftingTable(player: Player): Inventory {
        val inv = server.createInventory(player, 45, text("Crafting Table"))
        val arrow = ItemValue.placeholder(Material.ARROW, "<yellow><!italic>Craft!", "build")
        val air = ItemStack(Material.AIR)
        val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)

        val contents = arrayOf(
            glass, glass, glass, glass, glass, glass, glass, glass, glass,
            glass, air, air, air, glass, glass, glass, glass, glass,
            glass, air, air, air, glass, arrow, glass, air, glass,
            glass, air, air, air, glass, glass, glass, glass, glass,
            glass, glass, glass, glass, glass, glass, glass, glass, glass
        )
        inv.contents = contents
        return inv
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        if (!e.view.title().toLegacyString().contains("Crafting Table") || e.inventory == null)
            return
        var cancel = false
        for(slot in e.newItems.keys) {
            if(outputIndex == slot)
                cancel = true
        }
        e.isCancelled = cancel

        rebuildInventory(e.inventory, e.whoClicked as? Player ?: return)
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (!e.view.title().toLegacyString().contains("Crafting Table") || anyNull(e.inventory, e.clickedInventory) || e.view.topInventory != e.clickedInventory)
            return

        @OptIn(DevOnly::class)
        Debug.dumpObjectData(e)

        val inv = e.clickedInventory!!
        val player = e.whoClicked as? Player ?: return

        if(e.view.topInventory != inv)
            return

        val clickedIndex = e.slot
        if(clickedIndex == -1)
            return

        val modifiedOutput = clickedIndex == outputIndex

        val pickedOutput = modifiedOutput && e.action.name.containsAny("PICKUP", "MOVE")

        if(pickedOutput) {
            // collecting items used in recipe
            buildItem(e)

            // player picked up result item, rebuild inventory
            rebuildInventory(inv, player, false)
            return
        } else if(modifiedOutput) {
            // player tried to put item in result slot, we do not allow it
            e.isCancelled = true
            rebuildInventory(inv, player)
            return
        }

        if(!gridIndices.contains(clickedIndex))
            e.isCancelled = true

        rebuildInventory(inv, player)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (!e.view.title().toLegacyString().contains("Crafting Table"))
            return

        val p = e.player as? Player ?: return

        val listed = e.inventory.toList()
        val grid = collect(listed.subList(10, 12), listed.subList(19, 21), listed.subList(28, 30), listOf(listed[outputIndex] ?: ItemStack(Material.AIR)))

        for(item in grid) {
            if(item != null && !item.type.isAir)
                p.giveOrDrop(item)
        }
    }

    private fun rebuildInventory(inv: Inventory, viewer: Player, removeResult: Boolean = true) {
        val mc = viewer.macrocosm ?: return
        val grid = MutableList(9) { ItemStack(Material.AIR) }
        val invList = inv.toList()
        grid.addAll(invList.subList(10, 12))
        grid.addAll(invList.subList(19, 21))
        grid.addAll(invList.subList(28, 30))
        val matching = RecipeHandler.matchingRecipes(inv, mc)
        val recipe = matching.firstOrNull()
        if (recipe == null || grid.all { it == null || it.type.isAir }) {
            // remove item we tried to craft previously from output slot
            if(removeResult)
                inv.setItem(outputIndex, null)
            return
        }

        val (actualRecipe, _) = recipe
        val result = actualRecipe.resultItem()
        inv.setItem(outputIndex, result)
    }

    private fun buildItem(e: InventoryClickEvent) {
        val macrocosm = (e.whoClicked as Player).macrocosm ?: return
        val grid = MutableList(9) { ItemStack(Material.AIR) }
        val invList = e.view.topInventory.toList()
        grid.addAll(invList.subList(10, 12))
        grid.addAll(invList.subList(19, 21))
        grid.addAll(invList.subList(28, 30))

        val matching = RecipeHandler.matchingRecipes(e.view.topInventory, macrocosm)
        val recipe = matching.firstOrNull()
        if (recipe == null || grid.all { it == null || it.type.isAir }) {
            return
        }
        val indices = recipe.second

        var expAmount = .0

        // clearing grid
        // all checks complete, and now we can actually reduce the amount of items
        for ((index, pair) in indices) {
            val (item, amount) = pair
            expAmount += (item.amount / 6.4) * (item.rarity.ordinal + 1)
            item.amount -= amount
            if (item.amount <= 0)
                e.view.topInventory.setItem(index, null)
            else e.view.topInventory.setItem(index, item)
        }

        val player = e.whoClicked as Player
        player.macrocosm?.addSkillExperience(SkillType.CARPENTRY, expAmount)
    }

    private val gridIndices = listOf(
        10, 11, 12,
        19, 20, 21,
        28, 29, 30
    )
    private const val outputIndex = 25
    private const val buildBtnIndex = 23
}
