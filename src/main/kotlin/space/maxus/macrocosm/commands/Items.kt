package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import org.bukkit.Material
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.reforge.ReforgeRegistry
import space.maxus.macrocosm.text.comp

fun recombobulateCommand() = command("recombobulate") {
    requires { it.hasPermission(4) }
    runs {
        val item = player.inventory.itemInMainHand
        if(item.type == Material.AIR) {
            player.sendMessage(comp("<red>Hold the item you want to recombobulate!"))
            return@runs
        }
        val m = item.macrocosm
        m.upgradeRarity()
        player.inventory.setItemInMainHand(m.build())
    }
}

fun reforgeCommand() = command("reforge") {
    requires { it.hasPermission(4) }
    argument("reforge", StringArgumentType.word()) {
        suggestList {
            ReforgeRegistry.reforges.keys
        }

        runs {
            val item = player.inventory.itemInMainHand
            if(item.type == Material.AIR) {
                player.sendMessage(comp("<red>Hold the item you want to reforge!"))
                return@runs
            }
            val macrocosm = item.macrocosm
            val reforge = getArgument<String>("reforge")
            macrocosm.reforge(ReforgeRegistry.find(reforge)!!)
            player.inventory.setItemInMainHand(macrocosm.build())
        }
    }
}
