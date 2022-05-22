package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.*
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.resources.ResourceLocation
import org.bukkit.Material
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.PetItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.recipeBrowser
import space.maxus.macrocosm.recipes.recipeViewer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.macrocosm
import kotlin.math.roundToInt

fun allItems() = kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = comp("Item Browser")
    defaultPage = 0

    page(0) {
        placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE))
        val compound = createRectCompound<Identifier>(
            Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
            iconGenerator = {
                Registry.ITEM.find(it).build()!!
            },
            onClick = { e, it ->
                if (e.bukkitEvent.click.isLeftClick)
                    e.player.inventory.addItem(Registry.ITEM.find(it).build(e.player.macrocosm)!!)
                else
                    e.player.inventory.addItem(Registry.ITEM.find(it).build(e.player.macrocosm)!!.apply { amount = 64 })
                e.bukkitEvent.isCancelled = true
            }
        )
        compound.addContent(Registry.ITEM.iter().keys.toList())
        compound.sortContentBy { it.path }
        compoundScroll(
            Slots.RowOneSlotNine,
            ItemValue.placeholder(Material.ARROW, "<green>Next"), compound, scrollTimes = 4
        )
        compoundScroll(
            Slots.RowSixSlotNine,
            ItemValue.placeholder(Material.ARROW, "<green>Back"), compound, scrollTimes = 4, reverse = true
        )

    }
}

fun viewRecipeCommand() = command("viewrecipe") {
    argument("recipe", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            Registry.RECIPE.iter().keys
                .filter { it.path.contains(ctx.getArgumentOrNull<ResourceLocation>("recipe")?.path?.lowercase() ?: "") }
        }

        runs {
            val recipe = getArgument<ResourceLocation>("recipe").macrocosm
            player.openGUI(recipeViewer(recipe, player.macrocosm!!))
        }

    }
}

fun recipesCommand() = command("recipes") {
    runs {
        player.openGUI(recipeBrowser(player.macrocosm!!))
    }
}

fun skillExp() = command("skillexp") {
    requires { it.hasPermission(4) }
    argument("skill", StringArgumentType.word()) {
        suggestList { ctx ->
            SkillType.values().filter {
                it.name.contains(ctx.getArgumentOrNull<String>("skill") ?: "")
            }
        }

        argument("exp", DoubleArgumentType.doubleArg()) {
            runs {
                val skill = getArgument<String>("skill")
                val exp = getArgument<Double>("exp")
                player.macrocosm?.addSkillExperience(SkillType.valueOf(skill), exp)
            }
        }
    }
}

fun collAmount() = command("coll") {
    requires { it.hasPermission(4) }
    argument("coll", StringArgumentType.word()) {
        suggestList { ctx ->
            CollectionType.values().filter {
                it.name.contains(ctx.getArgumentOrNull<String>("coll") ?: "")
            }
        }

        argument("amount", IntegerArgumentType.integer(0)) {
            runs {
                player.macrocosm?.addCollectionAmount(
                    CollectionType.valueOf(getArgument("coll")),
                    getArgument("amount")
                )
            }
        }
    }
}


fun itemsCommand() = command("items") {
    requires { it.hasPermission(4) }
    runs {
        player.openGUI(allItems())
    }
}

fun summonCommand() = command("spawnmob") {
    requires { it.hasPermission(4) }
    argument("entity", ResourceLocationArgument.id()) {
        suggestListSuspending {
            Registry.ENTITY.iter().keys.filter { k -> k.path.contains(it.getArgumentOrNull<ResourceLocation>("entity")?.path ?: "") }
        }

        runs {
            Registry.ENTITY.find(getArgument<ResourceLocation>("entity").macrocosm).spawn(player.location)
        }
    }
}

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
    } catch (e: java.lang.IllegalArgumentException) {
        null
    }
}

fun statCommand() = command("stat") {
    requires { it.hasPermission(4) }

    argument("player", EntityArgument.player()) {
        argument("statistic", StringArgumentType.word()) {
            suggestList { ctx ->
                Statistic.values().map { it.name }
                    .filter { it.contains(ctx.getArgumentOrNull<String>("statistic")?.uppercase() ?: "") }
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
        val stats = player.macrocosm!!.stats()!!
        val (damage, crit) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
        val message = comp(
            """
            <yellow>You would deal <red>${damage.roundToInt()} ${if (crit) "<gold>critical " else ""}<yellow>damage!
            Offensive stats:
            <red>${stats.damage.roundToInt()} ❁ Damage
            ${stats.strength.roundToInt()} ❁ Strength
            <blue>${stats.critChance.roundToInt()}% ☣ Crit Chance
            ${stats.critDamage.roundToInt()}% ☠ Crit Damage
        """.trimIndent()
        )
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

fun spawnPetCommand() = command("spawnpet") {
    requires { it.hasPermission(4) }
    argument("pet", StringArgumentType.string()) {
        suggestListSuspending {
            it.source.playerOrException.bukkitEntity.macrocosm!!.ownedPets.keys.filter { pet ->
                pet.contains(it.getArgumentOrNull("pet") ?: "")
            }
        }

        runs {
            val pet = getArgument<String>("pet")
            val stored = player.macrocosm!!.ownedPets[pet]!!
            player.macrocosm!!.activePet?.despawn(player.macrocosm!!)
            player.macrocosm!!.activePet = Registry.PET.find(stored.id).spawn(player.macrocosm!!, pet)
        }
    }
}

fun addPetCommand() = command("addpet") {
    requires {
        it.hasPermission(4)
    }

    argument("type", ResourceLocationArgument.id()) {
        suggestListSuspending {
            Registry.ITEM.iter().filter { item -> item.value is PetItem }.keys.filter { k ->
                k.path.contains(it.getArgumentOrNull("type") ?: "")
            }
        }

        argument("rarity", StringArgumentType.word()) {
            suggestListSuspending {
                Rarity.values().map { rarity -> rarity.name.lowercase() }.filter { rarity ->
                    rarity.contains(it.getArgumentOrNull("rarity") ?: "")
                }
            }

            argument("level", IntegerArgumentType.integer(1, 100)) {
                runs {
                    val pet = getArgument<ResourceLocation>("type").macrocosm
                    val rarity = Rarity.valueOf(getArgument<String>("rarity").uppercase())
                    val level = getArgument<Int>("level")
                    player.macrocosm!!.addPet(pet, rarity, level, .0)
                }
            }
        }
    }
}

fun givePetItemCommand() = command("givepet") {
    requires {
        it.hasPermission(4)
    }

    argument("type", ResourceLocationArgument.id()) {
        suggestListSuspending {
            Registry.ITEM.iter().filter { item -> item.value is PetItem }.keys.filter { k ->
                k.path.contains(it.getArgumentOrNull("type") ?: "")
            }
        }

        argument("rarity", StringArgumentType.word()) {
            suggestListSuspending {
                Rarity.values().map { rarity -> rarity.name.lowercase() }.filter { rarity ->
                    rarity.contains(it.getArgumentOrNull("rarity") ?: "")
                }
            }

            argument("level", IntegerArgumentType.integer(1, 100)) {
                runs {
                    val pet = getArgument<ResourceLocation>("type").macrocosm
                    val rarity = Rarity.valueOf(getArgument<String>("rarity").uppercase())
                    val level = getArgument<Int>("level")
                    val stored = StoredPet(pet, rarity, level, .0)
                    val petItem = Registry.ITEM.find(pet) as PetItem
                    petItem.stored = stored
                    petItem.rarity = stored.rarity
                    player.inventory.addItem(petItem.build(player.macrocosm!!)!!)
                }
            }
        }
    }
}
