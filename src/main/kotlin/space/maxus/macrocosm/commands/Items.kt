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
import space.maxus.macrocosm.ability.AbilityRegistry
import space.maxus.macrocosm.enchants.EnchantmentRegistry
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.item.types.WitherBlade
import space.maxus.macrocosm.item.types.WitherScrollAbility
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.reforge.ReforgeRegistry
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
            macrocosm.addScroll(AbilityRegistry.find(reforge)!! as WitherScrollAbility)
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
            ReforgeRegistry.reforges.keys.filter {
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
            macrocosm!!.reforge(ReforgeRegistry.find(reforge)!!)
            player.inventory.setItemInMainHand(macrocosm.build(player.macrocosm))
        }
    }
}

fun enchantCommand() = command("enchantme") {
    requires { it.hasPermission(4) }
    argument("enchant", ResourceLocationArgument.id()) {
        suggestList { ctx ->
            EnchantmentRegistry.enchants.keys.filter {
                it.path.contains(
                    ctx.getArgumentOrNull<ResourceLocation>("enchant")?.path ?: ""
                )
            }
        }

        argument("level", IntegerArgumentType.integer(0)) {
            suggestList { ctx ->
                val name =
                    ctx.getArgumentOrNull<ResourceLocation>("enchant")?.macrocosm ?: return@suggestList listOf("!")
                val ench = EnchantmentRegistry.find(name) ?: return@suggestList listOf("!")
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
                macrocosm!!.enchant(EnchantmentRegistry.find(enchant)!!, level)
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
                ItemRegistry.items.keys.filter {
                    it.path.contains(
                        ctx.getArgumentOrNull<ResourceLocation>("item")?.path ?: ""
                    )
                }
            }

            runs {
                val player = getArgument<EntitySelector>("player").findSinglePlayer(this.nmsContext.source).bukkitEntity
                val item = getArgument<ResourceLocation>("item").macrocosm

                val stack = ItemRegistry.find(item).build(player.macrocosm) ?: ItemStack(Material.AIR)
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
