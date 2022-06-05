package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import net.axay.kspigot.gui.openGUI
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.RecipeMenu
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.SlayerQuest
import space.maxus.macrocosm.slayer.SlayerStatus
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.ui.slayerChooseMenu

fun testBossCommand() = command("spawnboss") {
    argument("id", StringArgumentType.string()) {
        argument("tier", IntegerArgumentType.integer(0, 6)) {
            runs {
                SlayerQuest(SlayerType.valueOf(getArgument("id")), getArgument("tier"), 0f, SlayerStatus.FAIL).summonBoss(player)
            }
        }
    }
}

fun testMaddoxMenuCommand() = command("slayermenu") {
    runs {
        player.openGUI(slayerChooseMenu(player.macrocosm!!))
    }
}
fun testSlayerCommand() = command("slayer") {
    argument("id", StringArgumentType.string()) {
        argument("tier", IntegerArgumentType.integer(0, 6)) {
            runs {
                player.macrocosm!!.startSlayerQuest(SlayerType.valueOf(getArgument("id")), getArgument("tier"))
            }
        }
    }
}

fun testStatsCommand() = command("stats") {
    runs {
        for (comp in player.macrocosm?.stats()?.formatFancy()!!) {
            player.sendMessage(comp)
        }
    }
}

fun testLevelUp() = command("skillup") {
    argument("skill", StringArgumentType.word()) {
        suggestList {
            SkillType.values().filter { sk -> sk.name.contains(it.getArgumentOrNull<String>("skill") ?: "") }
        }
        runs {
            val sk = SkillType.valueOf(getArgument("skill"))
            player.macrocosm?.sendSkillLevelUp(sk)
        }
    }
}

fun testCollUp() = command("collup") {
    argument("coll", StringArgumentType.word()) {
        suggestList {
            CollectionType.values().filter { sk -> sk.name.contains(it.getArgumentOrNull<String>("coll") ?: "") }
        }
        runs {
            val sk = CollectionType.valueOf(getArgument("coll"))
            player.macrocosm?.sendCollectionLevelUp(sk)
        }
    }
}

fun testCraftingTable() = command("crafting_test") {
    runs {
        this.player.openInventory(RecipeMenu.craftingTable(this.player))
    }
}
