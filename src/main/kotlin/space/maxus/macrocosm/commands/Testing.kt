package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.*
import net.axay.kspigot.commands.*
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation
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
import space.maxus.macrocosm.util.general.DevelopmentArgs
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.general.macrocosm

@OptIn(DevelopmentOnly::class)
fun setDevArg() = command("devarg") {
    argument("key", StringArgumentType.string()) {
        suggestListSuspending {
            DevelopmentArgs.required.filter { key -> key.contains(it.getArgumentOrNull("key") ?: "") }
        }

        argument("str", StringArgumentType.string()) {
            runsCatching {
                DevelopmentArgs[getArgument("key")] = getArgument<String>("str")
                player.sendMessage(text("<#691ff2>[Macrocosm]<aqua> Set the value of the <gold>${getArgument<String>("key")}</gold> development variable to a string <green>'${getArgument<String>("str")}'</green>"))
            }
        }

        argument("float", FloatArgumentType.floatArg()) {
            runsCatching {
                DevelopmentArgs[getArgument("key")] = getArgument<Float>("float")
                player.sendMessage(text("<#691ff2>[Macrocosm]<aqua> Set the value of the <gold>${getArgument<String>("key")}</gold> development variable to a float <green>${getArgument<Float>("float")}</green>"))
            }
        }

        argument("bool", BoolArgumentType.bool()) {
            runsCatching {
                DevelopmentArgs[getArgument("key")] = getArgument<Boolean>("bool")
                player.sendMessage(text("<#691ff2>[Macrocosm]<aqua> Set the value of the <gold>${getArgument<String>("key")}</gold> development variable to a bool <green>${getArgument<Boolean>("bool")}</green>"))
            }
        }
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
        jacobusUi(player.macrocosm!!).open(player)
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
        petsMenu(player.macrocosm!!).open(player)
    }
}

fun openBazaarMenuCommand() = command("bazaar") {
    runsCatching {
        globalBazaarMenu(player.macrocosm!!).open(player)
    }
}

fun openForgeMenuCommand() = command("openforge") {
    argument("id", StringArgumentType.string()) {
        runs {
            val ty = ForgeType.valueOf(getArgument("id"))
            displayForge(player.macrocosm!!, ty).open(player)
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
        slayerChooseMenu(player.macrocosm!!).open(player)
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
