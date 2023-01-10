package space.maxus.macrocosm.recipes

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.extensions.server
import net.axay.kspigot.runnables.task
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
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.anyNull
import space.maxus.macrocosm.util.containsAny
import space.maxus.macrocosm.util.general.collect
import space.maxus.macrocosm.util.giveOrDrop

@Suppress("SENSELESS_COMPARISON")
object RecipeMenu : Listener {
    private val result = ItemValue.placeholderDescripted(Material.BARRIER, "<red><!italic>Recipe Required")
    private val red = ItemValue.placeholder(Material.RED_STAINED_GLASS_PANE, "")
    private val green = ItemValue.placeholder(Material.GREEN_STAINED_GLASS_PANE, "")

    fun craftingTable(player: Player): Inventory {
        val inv = server.createInventory(player, 54, text("Crafting Table"))
        val air = ItemStack(Material.AIR)
        val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
        val back = ItemValue.placeholderDescripted(Material.ARROW, "<yellow><!italic>Back", "Return to Macrocosm menu")

        val contents = arrayOf(
            glass, glass, glass, glass, glass, glass, glass, glass, glass,
            glass, air, air, air, glass, glass, glass, glass, glass,
            glass, air, air, air, glass, result, glass, glass, glass,
            glass, air, air, air, glass, glass, glass, glass, glass,
            glass, glass, glass, glass, glass, glass, glass, glass, glass,
            red, red, red, red, back, red, red, red, red
        )
        inv.contents = contents
        return inv
    }

    @EventHandler
    fun onInventoryDrag(e: InventoryDragEvent) {
        if (!e.view.title().toLegacyString().contains("Crafting Table") || e.inventory == null)
            return
        var cancel = false
        for (slot in e.newItems.keys) {
            if (outputIndex == slot)
                cancel = true
        }
        e.isCancelled = cancel

        val inv = e.inventory
        rebuildInventory(
            e.inventory, e.whoClicked as? Player ?: return, collect(
                inv.getItems(10..12),
                inv.getItems(19..21),
                inv.getItems(28..30)
            )
        )
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (!e.view.title().toLegacyString().contains("Crafting Table") || anyNull(e.inventory, e.clickedInventory))
            return

        val inv = e.clickedInventory!!

        val player = e.whoClicked as? Player ?: return

        val grid: MutableList<ItemStack?> = collect(
            inv.getItems(10..12),
            inv.getItems(19..21),
            inv.getItems(28..30)
        )

        if (e.view.topInventory != inv) {
            task {
                rebuildInventory(inv, player, grid)
            }
            return
        }

        val clickedIndex = e.slot

        val modifiedOutput = clickedIndex == outputIndex

        val pickedOutput = modifiedOutput && e.action.name.containsAny("PICKUP", "MOVE")

        if (!gridIndices.contains(clickedIndex) && !modifiedOutput) {
            e.isCancelled = true
            return
        }


        if (gridIndices.contains(clickedIndex))
            grid[clickedToGrid(clickedIndex)] = e.cursor

        if (pickedOutput && inv.getItem(outputIndex)?.itemMeta?.persistentDataContainer?.has(pluginKey("placeholder")) != true) {
            // collecting items used in recipe
            collectIngredients(e)

            // player picked up result item, rebuild inventory
            rebuildInventory(inv, player, grid, false)
            return
        } else if (modifiedOutput) {
            // player tried to put item in result slot, we do not allow it
            e.isCancelled = true
            rebuildInventory(inv, player, grid)
            return
        }

        rebuildInventory(inv, player, grid, true)
    }

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        if (!e.view.title().toLegacyString().contains("Crafting Table"))
            return

        val p = e.player as? Player ?: return

        val inv = e.inventory
        val grid: MutableList<ItemStack?> = collect(
            inv.getItems(10..12),
            inv.getItems(19..21),
            inv.getItems(28..30)
        )

        for (item in grid) {
            if (item != null && !item.type.isAir)
                p.giveOrDrop(item)
        }
    }

    private fun rebuildInventory(inv: Inventory, viewer: Player, grid: List<ItemStack?>, removeResult: Boolean = true) {
        val mc = viewer.macrocosm ?: return
        val matching = RecipeHandler.matchingRecipes(inv, grid, mc)
        val recipe = matching.firstOrNull()
        if (recipe == null || grid.all { it == null || it.type.isAir }) {
            // remove item we tried to craft previously from output slot
            if (removeResult)
                inv.setItem(outputIndex, result)

            // make an overlay that we can not craft anything
            inv.setItems(45..48, red)
            inv.setItems(50..53, red)
            return
        }

        val (actualRecipe, _) = recipe
        val result = actualRecipe.resultItem()
        inv.setItems(45..48, green)
        inv.setItems(50..53, green)
        inv.setItem(outputIndex, result)
    }

    private fun collectIngredients(e: InventoryClickEvent) {
        val macrocosm = (e.whoClicked as Player).macrocosm ?: return
        val inv = e.inventory
        val grid: MutableList<ItemStack?> = collect(
            inv.getItems(10..12),
            inv.getItems(19..21),
            inv.getItems(28..30)
        )

        val matching = RecipeHandler.matchingRecipes(e.view.topInventory, grid, macrocosm)
        val recipe = matching.firstOrNull()
        if (recipe == null || grid.all { it == null || it.type.isAir }) {
            return
        }
        val indices = recipe.second

        // clearing grid
        // all checks complete, and now we can actually reduce the amount of items
        for ((index, pair) in indices) {
            val (item, amount) = pair
            item.amount -= amount
            if (item.amount <= 0)
                e.view.topInventory.setItem(index, null)
            else e.view.topInventory.setItem(index, item)
        }
    }

    private fun Inventory.getItems(at: IntRange) = at.map { getItem(it) }
    private fun Inventory.setItems(at: IntRange, item: ItemStack) = at.forEach {
        setItem(it, item)
    }

    private fun clickedToGrid(index: Int): Int = when (index) {
        10, 11, 12 -> index - 10
        19, 20, 21 -> index - 16
        28, 29, 30 -> index - 22
        else -> -1
    }

    private val gridIndices = listOf(
        10, 11, 12,
        19, 20, 21,
        28, 29, 30
    )
    private const val outputIndex = 23
}
