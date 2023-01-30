package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import net.axay.kspigot.gui.openGUI
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation
import org.bukkit.Location
import space.maxus.macrocosm.accessory.ui.jacobusUi
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.bazaar.ui.globalBazaarMenu
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.discord.emitters.HighSkillEmitter
import space.maxus.macrocosm.forge.ForgeType
import space.maxus.macrocosm.forge.ui.displayForge
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.pets.ui.petsMenu
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.RecipeMenu
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.ui.slayerChooseMenu
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.annotations.DevelopmentOnly
import space.maxus.macrocosm.util.general.Debug
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.general.macrocosm
import space.maxus.macrocosm.zone.PolygonalZone

private val vertices = mutableListOf<Location>()
private var testLocation: PolygonalZone? = null

fun addLocVertex() = command("locaddvertex") {
    runsCatching {
        val pos = player.location
        vertices.add(pos)
        player.sendMessage(text("<#691ff2>[Macrocosm]<aqua> Added a location vertex at <green>${pos.blockX} ${pos.blockY} ${pos.blockZ}"))
    }
}

fun finishLoc() = command("locfinish") {
    runsCatching {
        testLocation = PolygonalZone("_test", "test", vertices)
        vertices.clear()
        player.sendMessage(text("<#691ff2>[Macrocosm]<aqua> Finalized a test location, run <green>/locinside</green> to check if the player is inside this location!"))
    }
}

fun insideLoc() = command("locinside") {
    runsCatching {
        if(testLocation == null)
            return@runsCatching

        val contains = testLocation!!.contains(player.location)
        player.sendMessage(text("<#691ff2>[Macrocosm]<aqua> Does location contain a player? <green>$contains"))
    }
}

fun doTestEmitPost() = command("doemit") {
    runsCatching {
        Threading.runAsync {
            Registry.DISCORD_EMITTERS.tryUse(id("high_skill")) { emitter ->
                (emitter as HighSkillEmitter).post(
                    HighSkillEmitter.Context(
                        SkillType.MYSTICISM,
                        47,
                        player.macrocosm!!
                    )
                )
            }
        }
    }
}

fun testJacobus() = command("jacobustest") {
    runsCatching {
        player.openGUI(jacobusUi(player.macrocosm!!))
    }
}

@OptIn(DevelopmentOnly::class)
fun handDebug() = command("handdebug") {
    runsCatching {
        val mc = player.inventory.itemInMainHand.macrocosm!!
        val texts = Debug.constructObjectData(mc).split("\n")
        for (part in texts) {
            player.sendMessage(part)
        }
    }
}

fun petsCommand() = command("pets") {
    runsCatching {
        player.openGUI(petsMenu(player.macrocosm!!))
    }
}

fun openBazaarMenuCommand() = command("bazaar") {
    runsCatching {
        player.openGUI(globalBazaarMenu(player.macrocosm!!))
    }
}

fun openForgeMenuCommand() = command("openforge") {
    argument("id", StringArgumentType.string()) {
        runs {
            val ty = ForgeType.valueOf(getArgument("id"))
            player.openGUI(displayForge(player.macrocosm!!, ty))
        }
    }
}

fun setSlayerLevelCommand() = command("slayerlvl") {
    argument("id", StringArgumentType.string()) {
        argument("exp", IntegerArgumentType.integer(0, 9)) {
            runs {
                val ty = SlayerType.valueOf(getArgument("id"))
                val slayer = player.macrocosm!!.slayers[ty]!!
                player.macrocosm!!.slayers[ty] = SlayerLevel(getArgument("exp"), 0.0, listOf(), slayer.rng)
            }
        }
    }
}

fun addSlayerExpCommand() = command("slayerxp") {
    argument("id", StringArgumentType.string()) {
        argument("exp", DoubleArgumentType.doubleArg(.0)) {
            runs {
                val ty = SlayerType.valueOf(getArgument("id"))
                val slayer = player.macrocosm!!.slayers[ty]!!
                player.macrocosm!!.slayers[ty] =
                    SlayerLevel(slayer.level, slayer.overflow, slayer.collectedRewards, slayer.rng.apply {
                        this[ty]!!.expAccumulated += getArgument<Double>("exp")
                    })
            }
        }
    }
}

fun giveRecipeCommand() = command("giverecipe") {
    argument("recipe", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            Registry.RECIPE.iter().keys
                .filter { it.path.contains(ctx.getArgumentOrNull<ResourceLocation>("recipe")?.path?.lowercase() ?: "") }
        }

        runs {
            val recipe = getArgument<ResourceLocation>("recipe").macrocosm
            if (!Registry.RECIPE.has(recipe))
                return@runs
            val mc = player.macrocosm!!
            if (mc.unlockedRecipes.contains(recipe))
                return@runs
            mc.unlockedRecipes.add(recipe)
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
