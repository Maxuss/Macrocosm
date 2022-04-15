package space.maxus.macrocosm.commands

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import org.bukkit.Material
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.players.macrocosm

fun testItemCommand() = command("itemtest") {
    runs {
        player.inventory.addItem(VanillaItem(Material.NETHERITE_SWORD).build())
    }
}

fun testStatsCommand() = command("stats") {
    runs {
        for(comp in player.macrocosm?.calculateStats()?.formatFancy()!!) {
            player.sendMessage(comp)
        }
    }
}
