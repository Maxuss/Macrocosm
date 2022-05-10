package space.maxus.macrocosm.recipes

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.extensions.server
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.ctx.CraftingTableContext
import space.maxus.macrocosm.text.comp

@Suppress("SENSELESS_COMPARISON")
object RecipeMenu: Listener {
    fun craftingTable(player: Player): Inventory {
        val inv = server.createInventory(player, 45, comp("Crafting Table"))
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
    fun clickHandler(e: InventoryClickEvent) {
        if(!e.view.title().toLegacyString().contains("Crafting Table"))
            return
        if(e.currentItem != null && e.currentItem!!.itemMeta.persistentDataContainer.has(pluginKey("placeholder"))) {
            e.isCancelled = true
        }
        val new = e.currentItem
        val newIndex = e.view.topInventory.indexOf(new)
        if(newIndex == buildBtnIndex) {
            buildItem(e)
        }
    }

    private fun buildItem(e: InventoryClickEvent) {
        val macrocosm = (e.whoClicked as Player).macrocosm ?: return
        val grid = MutableList(9) { ItemStack(Material.AIR) }
        val invList = e.view.topInventory.toList()
        grid.addAll(invList.subList(10, 12))
        grid.addAll(invList.subList(19, 21))
        grid.addAll(invList.subList(28, 30))

        @Suppress("SENSELESS_COMPARISON")
        val ctx = CraftingTableContext(grid.map { if(it == null || it.type.isAir) VanillaItem(Material.AIR) else it.macrocosm!! })

        val matching = RecipeHandler.matchingRecipes(e.view.topInventory, macrocosm)
        // todo: handle multiple recipes
        val recipe = matching.firstOrNull()
        if(recipe == null || grid.all { it == null || it.type.isAir }) {
            macrocosm.sendMessage("<red>Could not find suitable recipe!")
            sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                pitch = 0f
                playFor(macrocosm.paper!!)
            }
            return
        }
        val resultItem = recipe.first.resultItem()
        val indices = recipe.second
        val resultSlot = e.view.topInventory.getItem(outputIndex)

        if(resultSlot != null && !resultSlot.type.isAir) {
            val id = resultSlot.macrocosm!!.id
            if(id != resultItem.macrocosm!!.id) {
                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                    pitch = 0f
                    playFor(macrocosm.paper!!)
                }
                return
            } else {
                if(resultSlot.amount + resultItem.amount <= resultItem.maxStackSize) {
                    sound(Sound.BLOCK_ANVIL_USE) {
                        playFor(macrocosm.paper!!)
                    }
                    resultSlot.amount += resultItem.amount
                    e.view.topInventory.setItem(outputIndex, resultSlot)
                } else {
                    sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                        pitch = 0f
                        playFor(macrocosm.paper!!)
                    }
                    return
                }
            }
        } else {
            val result = recipe.first.assemble(ctx, macrocosm)
            sound(Sound.BLOCK_ANVIL_USE) {
                playFor(macrocosm.paper!!)
            }
            e.view.topInventory.setItem(outputIndex, result)
        }
        // clearing grid
        // todo: give player carpentry experience on craft
        // all checks complete, and now we can actually reduce the amount of items
        for ((index, pair) in indices) {
            val (item, amount) = pair
            item.amount -= amount
            if (item.amount <= 0)
                e.view.topInventory.setItem(index, null)
            else e.view.topInventory.setItem(index, item)
        }
    }

    private val gridIndices = listOf(
        10, 11, 12,
        19, 20, 21,
        28, 29, 30
    )
    private const val outputIndex = 25
    private const val buildBtnIndex = 23
}
