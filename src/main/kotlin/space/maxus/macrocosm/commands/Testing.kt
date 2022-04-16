package space.maxus.macrocosm.commands

import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import space.maxus.macrocosm.players.macrocosm

fun testStatsCommand() = command("stats") {
    runs {
        for (comp in player.macrocosm?.calculateStats()?.formatFancy()!!) {
            player.sendMessage(comp)
        }
    }
}
