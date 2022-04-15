package space.maxus.macrocosm.commands

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.entity.VanillaEntity
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.players.macrocosm

fun testItemCommand() = command("itemtest") {
    runs {
        player.inventory.addItem(VanillaItem(Material.NETHERITE_SWORD).build() ?: ItemStack(Material.AIR))
    }
}

fun testStatsCommand() = command("stats") {
    runs {
        for (comp in player.macrocosm?.calculateStats()?.formatFancy()!!) {
            player.sendMessage(comp)
        }
    }
}

fun testEntityCommand() = command("entitytest") {
    runs {
        VanillaEntity.from(EntityType.WITHER_SKELETON, player.location)
    }
}
