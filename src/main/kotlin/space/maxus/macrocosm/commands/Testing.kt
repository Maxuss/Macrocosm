package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.resources.ResourceLocation
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.accessory.ui.jacobusUi
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.bazaar.ui.globalBazaarMenu
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.discord.emitters.HighSkillEmitter
import space.maxus.macrocosm.entity.textureProfile
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
import space.maxus.macrocosm.util.annotations.DevelopmentOnly
import space.maxus.macrocosm.util.general.Debug
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.general.macrocosm


fun doTestDisguise() = command("dodisguise") {
    runsCatching {
        val entity = world.spawnEntity(player.location, EntityType.SPIDER)
        DisguiseAPI.disguiseEntity(entity, PlayerDisguise(
            textureProfile(
            "ewogICJ0aW1lc3RhbXAiIDogMTYzNTY5OTQ4MTY3NywKICAicHJvZmlsZUlkIiA6ICIyYzEwNjRmY2Q5MTc0MjgyODRlM2JmN2ZhYTdlM2UxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYWVtZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jNDY5MDlmNjNhYmQ5YTQ1Y2IxMDU1NGJiMWQ2ZTAwMGIzYjExMmI3ZGIzOWQ5OTAxNDQxZTUyYTllZjNjMGRjIgogICAgfQogIH0KfQ==",
            "tIQmgDSbUvTnVbZMvhCm5wS3HbNUu4QxoWYEfrEfoVShO7sWg0V3MPH1lIVr29yege0vSfc6wITqOR0U6HwEGp8nZKvtHTIfdnSqYleOMU/4q2ZDyLo3xB3J0kNzQDYzjjyJ9V3A/vQnaNz9EmACJqj7aHk3kK/KpCTIDuCXXof2qiOZUuJIyebW3THl/VBl0hZTSVOxCDYqT6iUYbdhxBhEQKmc9ObBFtwKN2i3kGrpkFCYgntz2xSAdYq2rk6GF/dhREmdjkqiG8uqs17beaa6Jxjy2KWHKjiRUSHvigr20uyPanMwjk/tXq2NpfZMrwajYvKLuSBB3wsxNj6YRuUtpuqw3YLOny/JhbMzrZi+irbsSAb1vWHBeEn2VWcD0gwsRQ7nSx4i4m9yY4wF4+AZeSUFJohvROFgrifVS//Mw2pd8cDYTpjODTIxwOiISBcXKLEDIXGhJj9lkkAXVKpz2EX/hlrRzfgYRLCwYJns7MM1oYqnbB0bhBNBfSSEHyYbSJg1qJuV+fBvJk9td+ZNRQwSL5jyTvee1SoQyZLHOfdPwh8IuyMNPJyZAS8TyHdqudzFrSyU2OIBtqMqAS00SDq3Is6m5su3+UyO0CmUW723pK39jZrA9k0ZHz3S5720kTnxXqtL0Z7Qr1vGF40eFaZAxCDn+CRBX5Wqrck="
        )))
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
