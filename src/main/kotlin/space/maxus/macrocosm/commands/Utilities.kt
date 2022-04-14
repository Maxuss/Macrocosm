package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

fun playtimeCommand() = command("playtime") {
    runs {
        val mc = player.macrocosm!!
        var millis = mc.playtimeMillis()
        val hours = millis / (60 * 60 * 1000)
        millis -= hours * (60 * 60 * 1000)
        val minutes = millis / (60 * 1000)
        player.sendMessage(comp("<green>Your playtime is ${if(hours <= 0) "" else "${hours}h "}${minutes}m!"))
    }
}

fun statCommand() = command("stat") {
    requires { it.hasPermission(4) }

    argument("player", EntityArgument.player()) {
        argument("statistic", StringArgumentType.word()) {
            suggestList { Statistic.values().map { it.name } }

            argument("value", FloatArgumentType.floatArg()) {

                runs {
                    val player =
                        getArgument<EntitySelector>("player").findSinglePlayer(nmsContext.source).bukkitEntity.macrocosm
                    if (player == null) {
                        this.player.sendMessage(comp("<red>Provided player is not online!"))
                        return@runs
                    }
                    val statName = getArgument<String>("statistic")
                    println(statName)
                    val stat = Statistic.valueOf(statName)
                    println(stat)
                    val value = getArgument<Float>("value")
                    player.baseStats[stat] = value
                    this.player.sendMessage(comp("<green>Set $stat of player <gold>${player.paper?.name}<green> to $value!"))
                }
            }
        }
    }
}
