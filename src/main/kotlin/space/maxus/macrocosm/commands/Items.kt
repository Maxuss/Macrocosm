package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.selector.EntitySelector
import net.minecraft.resources.ResourceLocation
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.item.LimitedEditionItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.item.types.WitherBlade
import space.maxus.macrocosm.item.types.WitherScrollAbility
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import space.maxus.macrocosm.util.macrocosm

fun recombobulateCommand() = command("recombobulate") {
    requires { it.hasPermission(4) }
    runs {
        val item = player.inventory.itemInMainHand
        if (item.type == Material.AIR) {
            player.sendMessage(comp("<red>Hold the item you want to recombobulate!"))
            return@runs
        }
        val m = item.macrocosm!!
        m.upgradeRarity()
        player.inventory.setItemInMainHand(m.build(player.macrocosm))
    }
}

fun giveAdminItemCommand() = command("giveadminstuff") {
    requires { it.hasPermission(4) }
    argument("to", EntityArgument.player()) {
        argument("item", ResourceLocationArgument.id()) {
            suggestList {
                Registry.ITEM.iter().filter { (_, v) -> v is LimitedEditionItem }.keys
                    .filter { k -> k.path.contains(it.getArgumentOrNull<ResourceLocation>("item")?.path ?: "") }
            }

            runs {
                val id = getArgument<ResourceLocation>("item").macrocosm
                val item = Registry.ITEM.find(id)
                if (item !is LimitedEditionItem)
                    return@runs
                val to = getArgument<EntitySelector>("to").findSinglePlayer(this.nmsContext.source).bukkitEntity
                val toDisplay = to.displayName()
                val fromDisplay = player.displayName()
                val edition = Database.Limited.incrementGet(id)
                item.givenTo = toDisplay
                item.givenBy = fromDisplay
                item.edition = edition
                player.sendMessage(comp("<green>You have given ").append(toDisplay).append(comp("<green> #$edition ${item.nameStr}!")))
                to.sendMessage(fromDisplay.append(comp("<gold> has given you <red>#$edition ${item.nameStr}<gold>! <aqua><bold>WOW!")))
                to.inventory.addItem(item.build(to.macrocosm!!)!!)
            }
        }
    }
}

fun setStarsCommand() = command("setstars") {
    requires { it.hasPermission(4) }
    argument("stars", IntegerArgumentType.integer(0)) {
        runs {
            val item = player.inventory.itemInMainHand
            if (item.type == Material.AIR) {
                player.sendMessage(comp("<red>Hold the item you want to add scroll to!"))
                return@runs
            }
            val macrocosm = item.macrocosm ?: return@runs
            macrocosm.stars = getArgument("stars")
            player.inventory.setItemInMainHand(macrocosm.build(player.macrocosm))
        }
    }
}


fun addScrollCommand() = command("addscroll") {
    requires { it.hasPermission(4) }
    argument("scroll", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            listOf(id("implosion"), id("wither_shield"), id("shadow_warp"), id("wither_impact")).filter {
                it.path.contains(
                    ctx.getArgumentOrNull<ResourceLocation>("scroll")?.path ?: ""
                )
            }
        }

        runs {
            val item = player.inventory.itemInMainHand
            if (item.type == Material.AIR) {
                player.sendMessage(comp("<red>Hold the item you want to add scroll to!"))
                return@runs
            }
            val macrocosm = item.macrocosm as? WitherBlade ?: return@runs
            val reforge = getArgument<ResourceLocation>("scroll").macrocosm
            macrocosm.addScroll(Registry.ABILITY.find(reforge) as WitherScrollAbility)
            player.inventory.setItemInMainHand(macrocosm.build(player.macrocosm))
        }
    }
}

fun unlockGemsCommand() = command("unlockgems") {
    requires { it.hasPermission(4) }
    runs {
        val mc = player.macrocosm
        val item = player.inventory.itemInMainHand.macrocosm ?: return@runs
        for ((gem, _) in item.runes) {
            item.unlockRune(gem)
        }
        player.inventory.setItemInMainHand(item.build(mc)!!)
    }
}

fun addPotatoBooks() = command("addbooks") {
    requires { it.hasPermission(4) }
    argument("amount", IntegerArgumentType.integer(0)) {
        runs {
            val mc = player.macrocosm
            val item = player.inventory.itemInMainHand.macrocosm ?: return@runs
            item.addPotatoBooks(getArgument("amount"))
            player.inventory.setItemInMainHand(item.build(mc)!!)
        }
    }
}

fun setGemsCommand() = command("setgems") {
    requires { it.hasPermission(4) }
    argument("level", IntegerArgumentType.integer(0)) {
        runs {
            val mc = player.macrocosm
            val lvl = getArgument<Int>("level")
            val item = player.inventory.itemInMainHand.macrocosm ?: return@runs
            for ((gem, _) in item.runes) {
                item.addRune(gem, lvl)
            }
            player.inventory.setItemInMainHand(item.build(mc)!!)
        }
    }
}

fun reforgeCommand() = command("reforge") {
    requires { it.hasPermission(4) }
    argument("reforge", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            Registry.REFORGE.iter().keys.filter {
                it.path.contains(
                    ctx.getArgumentOrNull<ResourceLocation>("reforge")?.path ?: ""
                )
            }
        }

        runs {
            val item = player.inventory.itemInMainHand
            if (item.type == Material.AIR) {
                player.sendMessage(comp("<red>Hold the item you want to reforge!"))
                return@runs
            }
            val macrocosm = item.macrocosm
            val reforge = getArgument<ResourceLocation>("reforge").macrocosm
            macrocosm!!.reforge(Registry.REFORGE.find(reforge))
            player.inventory.setItemInMainHand(macrocosm.build(player.macrocosm))
        }
    }
}

fun enchantCommand() = command("enchantme") {
    requires { it.hasPermission(4) }
    argument("enchant", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            Registry.ENCHANT.iter().keys.filter {
                it.path.contains(
                    ctx.getArgumentOrNull<ResourceLocation>("enchant")?.path ?: ""
                )
            }
        }

        argument("level", IntegerArgumentType.integer(0)) {
            suggestList { ctx ->
                val name =
                    ctx.getArgumentOrNull<ResourceLocation>("enchant")?.macrocosm ?: return@suggestList listOf("!")
                val ench = Registry.ENCHANT.find(name)
                ench.levels.map { it.toString() }
            }

            runs {
                val item = player.inventory.itemInMainHand
                if (item.type == Material.AIR) {
                    player.sendMessage(comp("<red>Hold the item you want to enchant!"))
                    return@runs
                }
                val level = getArgument<Int>("level")
                val macrocosm = item.macrocosm
                val enchant = getArgument<ResourceLocation>("enchant").macrocosm
                macrocosm!!.enchant(Registry.ENCHANT.find(enchant), level)
                player.inventory.setItemInMainHand(macrocosm.build(player.macrocosm))
            }
        }
    }
}

fun itemCommand() = command("getitem") {
    requires { it.hasPermission(4) }
    argument("player", EntityArgument.player()) {
        argument("item", ResourceLocationArgument.id()) {
            suggestList { ctx ->
                Registry.ITEM.iter().keys.filter {
                    it.path.contains(
                        ctx.getArgumentOrNull<ResourceLocation>("item")?.path ?: ""
                    )
                }
            }

            runs {
                val player = getArgument<EntitySelector>("player").findSinglePlayer(this.nmsContext.source).bukkitEntity
                val item = getArgument<ResourceLocation>("item").macrocosm

                val stack = Registry.ITEM.find(item).build(player.macrocosm) ?: ItemStack(Material.AIR)
                player.inventory.addItem(stack)
                this.player.sendMessage(
                    comp(
                        "<green>Gave ${player.name} ${
                            MiniMessage.miniMessage().serialize(stack.displayName())
                        }<green>!"
                    )
                )
            }
        }
    }
}
