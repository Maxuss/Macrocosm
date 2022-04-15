package space.maxus.macrocosm.commands

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import org.bukkit.Material
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.text.comp

fun recombobulateCommand() = command("recombobulate") {
    runs {
        val item = player.inventory.itemInMainHand
        if(item.type == Material.AIR) {
            player.sendMessage(comp("<red>Hold an item you want to recombobulate!"))
            return@runs
        }
        val m = ItemRegistry.toMacrocosm(item)
        m.upgradeRarity()
        player.inventory.setItemInMainHand(m.build())
    }
}
