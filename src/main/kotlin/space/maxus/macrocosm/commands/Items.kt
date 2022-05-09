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
import space.maxus.macrocosm.enchants.EnchantmentRegistry
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.reforge.ReforgeRegistry
import space.maxus.macrocosm.text.comp
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
        player.inventory.setItemInMainHand(m.build())
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
            player.inventory.setItemInMainHand(macrocosm.build())
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
                val name = ctx.getArgumentOrNull<ResourceLocation>("enchant")?.macrocosm ?: return@suggestList listOf("!")
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
                player.inventory.setItemInMainHand(macrocosm.build())
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

                val stack = ItemRegistry.find(item).build() ?: ItemStack(Material.AIR)
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
