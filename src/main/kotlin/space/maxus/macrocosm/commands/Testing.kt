package space.maxus.macrocosm.commands

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import org.bukkit.Material
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.SimpleItem
import space.maxus.macrocosm.stats.stats

fun testItemCommand() = command("itemtest") {
    runs {
        val item = SimpleItem("Cool Item", Material.DIAMOND, Rarity.LEGENDARY)
        item.stats = stats {
            damage = 100f
            strength = 120f
            magicFind = 50f
            intelligence = 120f
            speed = 15f
            seaCreatureChance = 0.4f
            attackSpeed = 50.1f
        }

        player.inventory.addItem(item.build())
    }
}
