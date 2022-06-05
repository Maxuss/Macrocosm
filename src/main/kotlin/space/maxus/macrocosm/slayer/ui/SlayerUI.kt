package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.slayer.*
import space.maxus.macrocosm.slayer.colorFromTier
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.stripTags
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.roundToInt

fun slayerChooseMenu(player: MacrocosmPlayer) = kSpigotGUI(GUIType.FIVE_BY_NINE) {
    title = comp("Slayer Menu")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
        placeholder(Slots.RowFourSlotTwo rectTo Slots.RowFourSlotEight, ItemValue.placeholderDescripted(Material.COAL_BLOCK, "<yellow>Coming Soon", "<red>In development!"))

        // specific slayer buttons
        val cmp = createRectCompound<SlayerType>(Slots.RowFourSlotTwo, Slots.RowFourSlotEight, iconGenerator = {
            if(it.slayer.requirementCheck(player))
                ItemValue.placeholderDescripted(it.slayer.item, it.slayer.name, *it.slayer.description.reduceToList(20).toTypedArray())
            else
                ItemValue.placeholderDescripted(it.slayer.item, it.slayer.name, *it.slayer.description.reduceToList(20).toMutableList().apply { add(""); add(it.slayer.requirementString); add(""); add("<yellow>Click to view details.") }.toTypedArray(), it.slayer.id)
        }, onClick = { e, ty ->
            e.bukkitEvent.isCancelled = true
            if(!ty.slayer.requirementCheck(player)) {
                player.sendMessage("<red>You do not meet requirements to start this slayer quest!")
                player.sendMessage("${ty.slayer.name} ${ty.slayer.requirementString}")
                e.player.closeInventory()
                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                    pitch = 0f
                    playFor(e.player)
                }
            } else {
                e.player.openGUI(specificSlayerMenu(player, ty))
            }
        })
        cmp.addContent(SlayerType.values().toList())

        button(Slots.RowTwoSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close"), onClick = {
            it.bukkitEvent.isCancelled = true
            it.player.closeInventory()
        })
    }
}

fun specificSlayerMenu(player: MacrocosmPlayer, ty: SlayerType) = kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = comp(ty.slayer.name.stripTags())
    defaultPage = 0
    val slayer = ty.slayer

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val cmp = createRectCompound<Int>(Slots.RowFourSlotTwo, Slots.RowFourSlotEight, iconGenerator = {
            if(it >= 6)
                ItemValue.placeholderDescripted(Material.COAL, "<dark_aqua>${slayer.name.stripTags()} VI", "<dark_gray>Excruciating", "", *"Maddox doesn't seem to know how to summon <red>this<gray> boss!".reduceToList(20).toTypedArray())
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
                    if(player.slayerExperience[ty]!!.level < it + 2)
                        buffer.add("<red>Requires ${slayer.name.stripTags()} LVL ${it + 2}")
                }
                else
                    buffer.add("<yellow>Click to slay!")
                ItemValue.placeholderDescripted(if(it < 5) slayer.item else slayer.secondaryItem, "<${colorFromTier(it).asHexString()}>${slayer.name.stripTags()} ${roman(it)}", *buffer.toTypedArray())
            }
        }, onClick = { e, tier ->
            e.bukkitEvent.isCancelled = true
            if(tier < 6) {
                val cost = costFromTier(tier)
                if(player.purse < cost) {
                    e.player.closeInventory()
                    e.player.sendMessage(comp("<red>You don't have enough coins to start this quest!"))
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
        })
        cmp.addContent(slayer.tiers)

        // rewards
        button(Slots.RowTwoSlotTwo, ItemValue.placeholderDescripted(Material.GOLD_BLOCK, "<gold>Boss Rewards", "Rewards per level that you", "can get from this slayer.", "", "Your Level: <green>${player.slayerExperience[ty]!!.level}")) { ev ->
            ev.bukkitEvent.isCancelled = true
            // todo: rewards menu
        }

        // drops
        button(Slots.RowTwoSlotFour, ItemValue.placeholderDescripted(Material.NETHERITE_SCRAP, "<red>Boss Drops", "Drops that you can get", "from this slayer bosses")) { ev ->
            ev.bukkitEvent.isCancelled = true
            // todo: drops menu
        }

        // rng meter
        val rng = player.slayerExperience[ty]!!.rngMeter
        val tiles = min(ceil(rng * 20).roundToInt(), 15)

        placeholder(Slots.RowTwoSlotSeven, ItemValue.placeholderDescripted(Material.PAINTING, "<light_purple>RNG Meter", "Feeling unlucky? Kill high tier", "bosses to accumulate RNG meter", "points. Upon reaching 100%", "guarantees a <light_purple>Crazy Rare<gray> drop!", " ", "Your Meter:", "<gradient:dark_purple:light_purple><bold>" + "-".repeat(tiles) + "</gradient><gray>" + "-".repeat(15 - tiles) + " <light_purple>${ Formatting.stats((rng * 100).toBigDecimal())}%"))
    }
}

inline fun confirmationRedirect(crossinline then: (MacrocosmPlayer) -> Unit) = kSpigotGUI(GUIType.THREE_BY_NINE) {
    title = comp("Confirmation")
    defaultPage = 0
    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(Slots.RowTwoSlotThree, ItemValue.placeholder(Material.GREEN_CONCRETE, "<green>Confirm"), onClick = {
            it.bukkitEvent.isCancelled = true
            then(it.player.macrocosm!!)
        })
        button(Slots.RowTwoSlotSix, ItemValue.placeholder(Material.RED_CONCRETE, "<red>Cancel"), onClick = {
            it.bukkitEvent.isCancelled = true
            it.player.closeInventory()
        })
    }
}
