package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.axay.kspigot.commands.*
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.sound.sound
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.resources.ResourceLocation
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.accessory.ui.thaumaturgyUi
import space.maxus.macrocosm.api.APIPermission
import space.maxus.macrocosm.api.KeyManager
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.bazaar.BazaarIntrinsics
import space.maxus.macrocosm.bazaar.ops.BazaarOp
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.collections.ui.collectionUi
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.events.YearChangeEvent
import space.maxus.macrocosm.exceptions.MacrocosmThrowable
import space.maxus.macrocosm.item.*
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.EquipmentHandler
import space.maxus.macrocosm.players.banking.Transaction
import space.maxus.macrocosm.players.banking.transact
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.recipeBrowser
import space.maxus.macrocosm.recipes.recipeViewer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.ui.rewardsMenu
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.spell.ui.displayInfusionTable
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.game.Calendar
import space.maxus.macrocosm.util.general.Debug
import space.maxus.macrocosm.util.general.macrocosm
import space.maxus.macrocosm.util.runCatchingReporting
import java.net.InetAddress
import java.util.*
import kotlin.math.roundToInt

fun collectionsCommand() = command("collections") {
    runsCatching {
        player.openGUI(collectionUi(player.macrocosm!!))
    }
}

fun connectDiscordCommand() = command("discordauth") {
    argument("username", StringArgumentType.greedyString()) {
        runsCatching {
            val id = player.uniqueId
            Discord.hasBegunAuth(id).then { key ->
                player.sendMessage(text("<green>You have already begun the authentication process! Run <yellow><click:copy_to_clipboard:'/auth key:$key'><hover:show_text:'<yellow>Click to copy!'>/auth key:$key</hover></click> in the discord bot!"))
            }.otherwise {
                if (Discord.hasAuthenticated(id))
                    player.sendMessage(text("<green>You have already authenticated!"))
                else {
                    val buf = ByteArray(16)
                    Macrocosm.random.nextBytes(buf)
                    val key = Base64.getUrlEncoder().encodeToString(buf)
                    player.sendMessage(text("<green>You have begun the authentication process! Run <yellow><click:copy_to_clipboard:'/auth key:$key'><hover:show_text:'<yellow>Click to copy!'>/auth key:$key</hover></click> in the discord bot to link your accounts!"))
                    Discord.step1Auth(player.uniqueId, getArgument("username"), key)
                }
            }.call()
        }
    }
}

fun thaumaturgyTest() = command("thaum") {
    runsCatching {
        val mc = player.macrocosm!!
        player.openGUI(thaumaturgyUi(mc))
    }
}

fun accessoriesCommand() = command("accessories") {
    runsCatching {
        val mc = player.macrocosm!!
        player.openGUI(mc.accessoryBag.ui(mc))
    }
}

fun placeBlockCommand() = command("placeblock") {
    argument("block", ResourceLocationArgument.id()) {
        suggestListSuspending { ctx ->
            Registry.BLOCK.iter().keys.filter {
                it.path.contains(
                    ctx.getArgumentOrNull<ResourceLocation>(
                        "block"
                    )?.path ?: ""
                )
            }
        }

        runsCatching {
            val block = Registry.BLOCK.find(getArgument<ResourceLocation>("block").macrocosm)
            block.place(player, player.macrocosm!!, player.location)
        }
    }
}

fun bazaarOpCommand() = command("bazaarop") {
    requires { it.hasPermission(4) }

    argument("operation", StringArgumentType.word()) {
        suggestStrings(BazaarOp.values().map { it.toString() })

        argument("item", ResourceLocationArgument.id()) {
            val keys = BazaarElement.allKeys
            suggestIds(keys)

            argument("quantity", IntegerArgumentType.integer(0)) {
                runsCatching {
                    val op = BazaarOp.valueOf(getArgument("operation"))
                    val item = getArgument<ResourceLocation>("item").macrocosm
                    val quantity = getArgument<Int>("quantity")
                    val mc = player.macrocosm!!
                    when (op) {
                        BazaarOp.DO_INSTANT_BUY -> {
                            Bazaar.instantBuy(mc, player, item, quantity)
                        }

                        BazaarOp.DO_INSTANT_SELL -> {
                            if (!BazaarIntrinsics.ensurePlayerHasEnoughItems(mc, player, item, quantity))
                                throw MacrocosmThrowable(
                                    "NOT_ENOUGH_ITEMS",
                                    "You do not have enough items of type $item in your inventory!"
                                )
                            Bazaar.instantSell(mc, player, item, quantity)
                        }

                        else -> {
                            throw MacrocosmThrowable(
                                "INVALID_ARGUMENTS",
                                "Invalid arguments provided, expected `pricePer`!"
                            )
                        }
                    }
                }

                argument("pricePer", DoubleArgumentType.doubleArg(0.1)) {
                    runsCatching {
                        val op = BazaarOp.valueOf(getArgument("operation"))
                        val item = getArgument<ResourceLocation>("item").macrocosm
                        val quantity = getArgument<Int>("quantity")
                        val pricePer = getArgument<Double>("pricePer")
                        val mc = player.macrocosm!!
                        when (op) {
                            BazaarOp.DO_INSTANT_BUY -> {
                                Bazaar.instantBuy(mc, player, item, quantity)
                            }

                            BazaarOp.DO_INSTANT_SELL -> {
                                if (!BazaarIntrinsics.ensurePlayerHasEnoughItems(mc, player, item, quantity))
                                    throw MacrocosmThrowable(
                                        "NOT_ENOUGH_ITEMS",
                                        "You do not have enough items of type $item in your inventory!"
                                    )
                                Bazaar.instantSell(mc, player, item, quantity)
                            }

                            BazaarOp.CREATE_BUY_ORDER -> {
                                Bazaar.createBuyOrder(mc, player, item, quantity, pricePer)
                            }

                            BazaarOp.CREATE_SELL_ORDER -> {
                                if (!BazaarIntrinsics.ensurePlayerHasEnoughItems(mc, player, item, quantity))
                                    throw MacrocosmThrowable(
                                        "NOT_ENOUGH_ITEMS",
                                        "You do not have enough items of type $item in your inventory!"
                                    )
                                Bazaar.createSellOrder(mc, player, item, quantity, pricePer)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun infusionCommand() = command("infuse") {
    runs {
        try {
            player.openGUI(displayInfusionTable(player.macrocosm!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun essenceCommand() = command("essence") {
    argument("essence", StringArgumentType.word()) {
        suggestList { ctx ->
            EssenceType.values()
                .filter { it.name.contains(ctx.getArgumentOrNull<String>("essence")?.uppercase() ?: "") }
                .map { it.name }
        }

        argument("amount", IntegerArgumentType.integer()) {
            runs {
                player.macrocosm!!.availableEssence[EssenceType.valueOf(
                    StringArgumentType.getString(
                        nmsContext,
                        "essence"
                    )
                )] = getArgument("amount")
            }
        }
    }
}

fun apiCommand() = command("api") {
    literal("new") {
        runs {
            val key = KeyManager.generateRandomKey(
                player.uniqueId,
                listOf(APIPermission.VIEW_BAZAAR_DATA, APIPermission.VIEW_PLAYER_DATA)
            )
            player.sendMessage(text("<green>Your new Macrocosm API Key is <click:copy_to_clipboard:$key><yellow><hover:show_text:'<green>Click to copy to clipboard'>$key</hover></click><green>!"))
        }
    }
    runs {
        player.sendMessage(text("<green>Macrocosm API documentation is located <click:open_url:'http://${InetAddress.getLocalHost().hostAddress}:4343/doc'><yellow><hover:show_text:'<gray>Click to open documentation!'>here</click><green>!"))
    }
}

fun getSlayerRewardsCommand() = command("slayerrewards") {
    argument("type", StringArgumentType.word()) {
        runs {
            val type = SlayerType.valueOf(getArgument("type"))
            player.openGUI(rewardsMenu(player.macrocosm!!, type))
        }
    }
}

fun setDateCommand() = command("date") {
    requires { it.hasPermission(4) }
    argument("date", IntegerArgumentType.integer(1, 21)) {
        runs {
            Calendar.ticksSinceDateChange = 0
            Calendar.date = getArgument("date")
        }

        argument("season", StringArgumentType.word()) {
            suggestList {
                Calendar.Season.values().map { it.name }
                    .filter { s -> s.contains(it.getArgumentOrNull("season") ?: "") }
            }

            runs {
                Calendar.ticksSinceDateChange = 0
                Calendar.date = getArgument("date")
                Calendar.season = Calendar.Season.valueOf(getArgument("season"))
            }

            argument("state", StringArgumentType.word()) {
                suggestList {
                    Calendar.SeasonState.values().map { it.name }
                        .filter { s -> s.contains(it.getArgumentOrNull("state") ?: "") }
                }

                runs {
                    Calendar.ticksSinceDateChange = 0
                    Calendar.date = getArgument("date")
                    Calendar.season = Calendar.Season.valueOf(getArgument("season"))
                    Calendar.state = Calendar.SeasonState.valueOf(getArgument("state"))
                }

                argument("year", IntegerArgumentType.integer(0)) {
                    runs {
                        Calendar.date = getArgument("date")
                        Calendar.season = Calendar.Season.valueOf(getArgument("season"))
                        Calendar.state = Calendar.SeasonState.valueOf(getArgument("state"))
                        val old = Calendar.year
                        Calendar.year = getArgument("year")
                        val event = YearChangeEvent(Calendar.year, old)
                        event.callEvent()
                        Calendar.ticksSinceDateChange = 0
                    }
                }
            }
        }
    }
}

fun payCommand() = command("pay") {
    argument("who", EntityArgument.player()) {
        argument("amount", DoubleArgumentType.doubleArg(.0)) {
            runsCatching {
                val from = player.macrocosm!!
                val to = getArgument<EntitySelector>("who").findSinglePlayer(nmsContext.source).bukkitEntity.macrocosm!!
                val amount = getArgument<Double>("amount")
                if (from.purse < amount.toBigDecimal()) {
                    from.sendMessage("<red>You don't have enough coins!")
                    return@runsCatching
                }
                from.purse -= transact(amount.toBigDecimal(), from.ref, Transaction.Kind.OUTGOING)
                to.purse += transact(amount.toBigDecimal(), to.ref, Transaction.Kind.INCOMING)
                from.sendMessage(
                    "<green>You've paid ${
                        to.paper!!.displayName().str()
                    } ${Formatting.withCommas(amount.toBigDecimal())}<green> coins!"
                )
                to.paper!!.sendMessage(
                    from.paper!!.displayName()
                        .append(text("<green> has just paid you ${Formatting.withCommas(amount.toBigDecimal())} coins!"))
                )
                sound(Sound.ENTITY_VILLAGER_YES) {
                    playFor(to.paper!!)
                    playFor(from.paper!!)
                }
            }
        }
    }
}

fun openEquipmentCommand() = command("equipment") {
    runs {
        EquipmentHandler.menu(player.macrocosm!!)
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

fun cosmeticCommand() = command("cosmetic") {
    requires { it.hasPermission(4) }

    argument("type", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            Registry.COSMETIC.iter().keys
                .filter { it.path.contains(ctx.getArgumentOrNull<ResourceLocation>("type")?.path?.lowercase() ?: "") }
        }

        runs {
            val cosmetic = Registry.COSMETIC.find(getArgument<ResourceLocation>("type").macrocosm)
            val mc = player.inventory.itemInMainHand.macrocosm!!
            if (cosmetic is Dye) {
                mc.addDye(cosmetic)
            } else if (cosmetic is SkullSkin) {
                mc.addSkin(cosmetic)
            }
            player.inventory.setItemInMainHand(mc.build(player.macrocosm!!)!!)
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
            runsCatching {
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
        player.openGUI(allItems(player))
    }
}

fun summonCommand() = command("spawnmob") {
    requires { it.hasPermission(4) }
    argument("entity", ResourceLocationArgument.id()) {
        suggestList {
            Registry.ENTITY.iter().keys.filter { k ->
                k.path.contains(
                    it.getArgumentOrNull<ResourceLocation>("entity")?.path ?: ""
                )
            }
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
        player.sendMessage(text("<green>Your playtime is ${if (hours <= 0) "" else "${hours}h "}${minutes}m!"))
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
                        this.player.sendMessage(text("<red>Provided player is not online!"))
                        return@runs
                    }
                    val statName = getArgument<String>("statistic")
                    val stat = Statistic.valueOf(statName)
                    val value = getArgument<Float>("value")
                    player.baseStats[stat] = value
                    this.player.sendMessage(text("<green>Set $stat of player <gold>${player.paper?.name}<green> to $value!"))
                }
            }
        }
    }
}

fun myDamageCommand() = command("mydamage") {
    runs {
        val stats = player.macrocosm!!.stats()!!
        val (damage, crit) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
        val message = text(
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
                    this.player.sendMessage(text("<red>Provided player is not online!"))
                    return@runs
                }
                val amount = getArgument<Int>("amount")
                player.purse += amount.toBigDecimal()
                this.player.sendMessage(text("<green>Successfully gave ${player.paper?.name} <gold>$amount coins<green>!"))
            }
        }
    }

}

fun spawnPetCommand() = command("spawnpet") {
    requires { it.hasPermission(4) }
    argument("pet", StringArgumentType.string()) {
        suggestList {
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
        suggestList {
            Registry.ITEM.iter().filter { item -> item.value is PetItem }.keys.filter { k ->
                k.path.contains(it.getArgumentOrNull("type") ?: "")
            }
        }

        argument("rarity", StringArgumentType.word()) {
            suggestList {
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

fun announceItemsCommand() = command("announceitems") {
    requires { it.hasPermission(4) }

    argument("id", ResourceLocationArgument.id()) {
        runs {
            val item = getArgument<ResourceLocation>("id").macrocosm
            Discord.sendItemDiffs(item)
        }
    }
}

fun givePetItemCommand() = command("givepet") {
    requires {
        it.hasPermission(4)
    }

    argument("type", ResourceLocationArgument.id()) {
        suggestList {
            Registry.ITEM.iter().filter { item -> item.value is PetItem }.keys.filter { k ->
                k.path.contains(it.getArgumentOrNull("type") ?: "")
            }
        }

        argument("rarity", StringArgumentType.word()) {
            suggestList {
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

fun addSpellCommand() = command("addspell") {
    requires { it.hasPermission(4) }

    argument("id", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            Registry.SPELL.iter().keys.filter { k ->
                k.path.contains(ctx.getArgumentOrNull<ResourceLocation>("id")?.path ?: "")
            }
        }
        runs {
            val id = ResourceLocationArgument.getId(nmsContext, "id").macrocosm
            val spell = Registry.SPELL.find(id)
            val scroll = player.inventory.itemInMainHand
            val sc = scroll.macrocosm!! as SpellScroll
            sc.spell = spell
            player.inventory.setItemInMainHand(sc.build(player.macrocosm!!))
        }
    }
}

fun allItems(player: Player) = kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = text("Item Browser")
    defaultPage = 0
    val mc = player.macrocosm!!

    page(0) {
        placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE))
        val compound = createRectCompound<Identifier>(
            Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
            iconGenerator = {
                try {
                    Registry.ITEM.find(it).build(mc)!!
                } catch (e: Exception) {
                    ItemValue.NULL.item.build()!!
                }
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
            ItemValue.placeholder(Material.ARROW, "<green>Next"), compound, scrollTimes = 1
        )
        compoundScroll(
            Slots.RowSixSlotNine,
            ItemValue.placeholder(Material.ARROW, "<green>Back"), compound, scrollTimes = 1, reverse = true
        )

    }
}

fun RequiredArgumentBuilder<CommandSourceStack, ResourceLocation>.suggestIds(keys: Iterable<Identifier>) {
    suggestList {
        keys.filter { key ->
            key.path.contains(
                it.getArgumentOrNull<ResourceLocation>(this.name)?.path ?: ""
            )
        }
    }
}

fun RequiredArgumentBuilder<CommandSourceStack, String>.suggestStrings(keys: Iterable<String>) {
    suggestList { keys.filter { key -> key.contains(it.getArgumentOrNull<String>(this.name) ?: "") } }
}

inline fun ArgumentBuilder<CommandSourceStack, *>.runsCatching(crossinline executor: CommandContext.() -> Unit) {
    runs {
        runCatchingReporting(player) {
            executor(this)
        }
    }
}
