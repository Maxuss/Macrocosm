package space.maxus.macrocosm.commands

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.entity.loot.DropRarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.RecipeMenu

fun testStatsCommand() = command("stats") {
    runs {
        for (comp in player.macrocosm?.calculateStats()?.formatFancy()!!) {
            player.sendMessage(comp)
        }
    }
}

fun testDropCommand() = command("raredrop") {
    runs {
        val item = ItemStack(Material.NETHER_STAR).macrocosm!!
        DropRarity.UNBELIEVABLE.announceEntityDrop(player, item)
        player.location.world.dropItemNaturally(player.location, item.build()!!)
    }
}

fun testCraftingTable() = command("crafting_test") {
    runs {
        this.player.openInventory(RecipeMenu.craftingTable(this.player))
    }
}
