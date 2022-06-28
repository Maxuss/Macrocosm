package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.colorFromTier
import space.maxus.macrocosm.slayer.costFromTier
import space.maxus.macrocosm.slayer.rewardExperienceForTier
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.renderBar
import space.maxus.macrocosm.util.stripTags

fun specificSlayerMenu(player: MacrocosmPlayer, ty: SlayerType): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = text(ty.slayer.name.stripTags())
    defaultPage = 0
    val slayer = ty.slayer

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val cmp = createCompound<Int>(iconGenerator = {
            if(it >= 6)
                ItemValue.placeholderDescripted(Material.COAL, "<dark_aqua>${slayer.name.stripTags()} VI", "<dark_gray>Excruciating", "", *"Maddox doesn't seem to know how to summon <red>this<gray> boss!".reduceToList(20).toTypedArray(), "<yellow>Oh... Okay.")
            else {
                val buffer = mutableListOf<String>()
                buffer.add("<dark_gray>" + slayer.difficulties[it - 1])
                buffer.add("")
                slayer.abilitiesForTier(it).map { abil -> abil.descript(it).map { e -> e.str() } }.forEach { l -> l.forEach { v -> buffer.add(v) }; buffer.add("") }
                buffer.add("Cost to start: <gold>${Formatting.stats(costFromTier(it).toBigDecimal())} Coins")
                buffer.add("")
                buffer.add("Slayer EXP: <red>${Formatting.stats(rewardExperienceForTier(it).toBigDecimal())} EXP")
                buffer.add("  <green>And extra drops!")
                buffer.add("")
                if(it >= 5) {
                    if(player.slayers[ty]!!.level < it + 2)
                        buffer.add("<red>Requires ${slayer.name.stripTags()} LVL ${it + 2}")
                }
                else
                    buffer.add("<yellow>Click to slay!")
                ItemValue.placeholderDescripted(if(it < 5) slayer.item else slayer.secondaryItem, "<${colorFromTier(it).asHexString()}>${slayer.name.stripTags()} ${roman(it)}", *buffer.toTypedArray())
            }
        }, onClick = { e, tier ->
            e.bukkitEvent.isCancelled = true
            if(tier < 6) {
                if(tier == 5 && player.slayers[ty]!!.level < 7) {
                    player.sendMessage("<red>You do not meet requirements to start this quest!")
                    e.player.closeInventory()
                } else {
                    val cost = costFromTier(tier)
                    if (player.purse < cost) {
                        e.player.closeInventory()
                        e.player.sendMessage(text("<red>You don't have enough coins to start this quest!"))
                        sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                            pitch = 0f
                            playFor(e.player)
                        }
                    } else {
                        e.player.openGUI(confirmationRedirect { p ->
                            p.purse -= cost.toFloat()
                            p.startSlayerQuest(ty, tier)
                            p.paper!!.closeInventory()
                        })
                    }
                }
            }
        })
        compoundSpace(LinearInventorySlots(listOf(InventorySlot(5, 3), InventorySlot(5, 4), InventorySlot(5, 5), InventorySlot(5, 6), InventorySlot(5, 7), InventorySlot(4, 5))), cmp)
        cmp.addContent(slayer.tiers)

        // rewards
        button(Slots.RowTwoSlotThree, ItemValue.placeholderDescripted(Material.GOLD_BLOCK, "<gold>Boss Rewards", "Rewards per level that you", "can get from this slayer.", "", "Your Level: <green>${player.slayers[ty]!!.level}")) { ev ->
            ev.bukkitEvent.isCancelled = true
            ev.player.openGUI(rewardsMenu(player, ty))
        }

        // drops
        button(Slots.RowTwoSlotFive, ItemValue.placeholderDescripted(Material.NETHERITE_SCRAP, "<red>Boss Drops", "Drops that you can get", "from this slayer bosses")) { ev ->
            ev.bukkitEvent.isCancelled = true
            ev.player.openGUI(dropsMenu(player, ty))
        }

        // rng meter
        val rng = player.slayers[ty]!!.rngMeter.toFloat()

        placeholder(
            Slots.RowTwoSlotSeven, ItemValue.placeholderDescripted(
                Material.PAINTING, "<light_purple>RNG Meter", "Feeling unlucky? Kill high tier", "bosses to accumulate RNG meter", "points. Upon reaching 100%", "guarantees a <light_purple>Crazy Rare<gray> drop!", " ", "Your Meter:", renderBar(rng, 15,
                    NamedTextColor.DARK_PURPLE, NamedTextColor.LIGHT_PURPLE) + " <light_purple>${Formatting.stats((rng * 100).toBigDecimal())}%"))

        button(Slots.RowOneSlotOne, ItemValue.placeholder(Material.ARROW, "<red>Back")) { ev ->
            ev.bukkitEvent.isCancelled = true
            ev.player.openGUI(slayerChooseMenu(player))
        }
    }
}
