package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.*
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp
import kotlin.math.roundToInt

fun playtimeCommand() = command("playtime") {
    runs {
        val mc = player.macrocosm!!
        var millis = mc.playtimeMillis()
        val hours = millis / (60 * 60 * 1000)
        millis -= hours * (60 * 60 * 1000)
        val minutes = millis / (60 * 1000)
        player.sendMessage(comp("<green>Your playtime is ${if (hours <= 0) "" else "${hours}h "}${minutes}m!"))
    }
}

inline fun <reified T> com.mojang.brigadier.context.CommandContext<CommandSourceStack>.getArgumentOrNull(name: String): T? {
    return try {
        getArgument<T>(name)
    } catch(e: java.lang.IllegalArgumentException) {
        null
    }
}

fun statCommand() = command("stat") {
    requires { it.hasPermission(4) }

    argument("player", EntityArgument.player()) {
        argument("statistic", StringArgumentType.word()) {
            suggestList {  ctx ->
                Statistic.values().map { it.name }.filter { it.contains(ctx.getArgumentOrNull<String>("statistic")?.uppercase() ?: "") }
            }

            argument("value", FloatArgumentType.floatArg()) {

                runs {
                    val player =
                        getArgument<EntitySelector>("player").findSinglePlayer(nmsContext.source).bukkitEntity.macrocosm
                    if (player == null) {
                        this.player.sendMessage(comp("<red>Provided player is not online!"))
                        return@runs
                    }
                    val statName = getArgument<String>("statistic")
                    val stat = Statistic.valueOf(statName)
                    val value = getArgument<Float>("value")
                    player.baseStats[stat] = value
                    this.player.sendMessage(comp("<green>Set $stat of player <gold>${player.paper?.name}<green> to $value!"))
                }
            }
        }
    }
}

fun myDamageCommand() = command("mydamage") {
    runs {
        val stats = player.macrocosm!!.calculateStats()!!
        val (damage, crit) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
        val message = comp("""
            <yellow>You would deal <red>${damage.roundToInt()} ${if(crit) "<gold>critical " else ""}<yellow>damage!
            Offensive stats:
            <red>${stats.damage.roundToInt()} ❁ Damage
            ${stats.strength.roundToInt()} ❁ Strength
            <blue>${stats.critChance.roundToInt()}% ☣ Crit Chance
            ${stats.critDamage.roundToInt()}% ☠ Crit Damage
        """.trimIndent())
        player.sendMessage(message)
    }
}

fun giveCoinsCommand() = command("givecoins") {
    requires { it.hasPermission(4) }
    argument("player", EntityArgument.player()) {
        argument("amount", IntegerArgumentType.integer(0)) {
            runs {
                val player =
                    getArgument<EntitySelector>("player").findSinglePlayer(nmsContext.source).bukkitEntity.macrocosm
                if (player == null) {
                    this.player.sendMessage(comp("<red>Provided player is not online!"))
                    return@runs
                }
                val amount = getArgument<Int>("amount")
                player.purse += amount
                this.player.sendMessage(comp("<green>Successfully gave ${player.paper?.name} <gold>$amount coins<green>!"))
            }
        }
    }

}
