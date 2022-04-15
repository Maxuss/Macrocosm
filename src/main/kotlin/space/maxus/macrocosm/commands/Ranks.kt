package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import net.axay.kspigot.extensions.bukkit.toComponent
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.text.comp

fun rankCommand() = command("rank") {
    requires { it.hasPermission(4) }

    argument("player", EntityArgument.player()) {
        argument("rank", StringArgumentType.word()) {

            suggestList {
                Rank.values().map { it.toString() }
            }
            runs {
                val playerSelector: EntitySelector = getArgument("player")
                val rank: Rank = Rank.valueOf(getArgument("rank"))
                val player = playerSelector.findSinglePlayer(nmsContext.source).bukkitEntity.macrocosm!!
                player.rank = rank

                val displayName = player.rank.playerName(player.paper?.name ?: "null")
                player.paper?.playerListName(player.paper?.name?.toComponent()?.color(player.rank.color))
                player.paper?.displayName(displayName)

                this.player.sendMessage(comp("<green>Set rank of player <gold>${player.paper?.name}<green> to $rank"))
            }
        }
    }
}
