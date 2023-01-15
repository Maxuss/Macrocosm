package space.maxus.macrocosm.accessory.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.accessory.power.PowerStone
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.giveOrDrop

object LearnPower: Listener {

    private val slots = listOf(13, 14, 15, 22, 23, 24, 31, 32, 33)
    @EventHandler(ignoreCancelled = true)
    fun onClick(e: InventoryClickEvent) {
        val clicker = e.whoClicked
        if(e.view.title().str() != "Learn Power From Stones" || e.clickedInventory == e.view.topInventory || clicker !is Player)
            return
        e.isCancelled = true
        val clickedItem = e.currentItem
        clickedItem?.amount = 1
        val clicked = clickedItem?.macrocosm
        if(clicked == null || clicked !is PowerStone)
            return

        e.clickedInventory!!.clear(e.slot)
        val top = e.view.topInventory
        clicker.openGUI(learnPowerUi(clicker.macrocosm!!, slots.mapNotNull { top.getItem(it)?.macrocosm as? PowerStone }.toMutableList().apply {
            add(clicked)
        }))
    }

    @EventHandler
    fun onClose(e: InventoryCloseEvent) {
        if(e.reason == InventoryCloseEvent.Reason.PLUGIN)
            return
        val clicker = e.player
        if(clicker !is Player || e.view.title().str() != "Learn Power From Stones")
            return
        for(slot in slots) {
            clicker.giveOrDrop(e.view.topInventory.getItem(slot) ?: continue)
        }
    }
}

fun learnPowerUi(player: MacrocosmPlayer, stones: MutableList<PowerStone> = mutableListOf()): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Learn Power From Stones")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(Slots.RowOneSlotFive, ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To Accessory Bag Thaumaturgy")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(thaumaturgyUi(player))
        }
        button(Slots.RowOneSlotSix,
            ItemValue.placeholderDescripted(
                Material.KNOWLEDGE_BOOK,
                "<green>Power Stones Guide",
                "View all power stones and how to",
                "acquire them!",
                "",
                "<yellow>Click to browse!")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.closeInventory(InventoryCloseEvent.Reason.UNKNOWN)
            e.player.openGUI(powerStonesGuide(player))
        }

        val distinct = stones.map { it.id }.distinct()
        val powerId = stones.firstOrNull()?.powerId
        val power = Registry.ACCESSORY_POWER.findOrNull(powerId)
        val combatReq = if(power is StoneAccessoryPower) power.combatLevel else 0
        if(distinct.size == 1 && stones.size == 9 && !player.memory.knownPowers.contains(powerId) && player.skills.level(SkillType.COMBAT) >= combatReq) {
            button(
                Slots.RowFourSlotThree, ItemValue.placeholderHeadDesc(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNjMjQ3YjBkODJkZDg3MTQ4Yzk2NTZhNDJlMDI0MDcwYzQ1OTcwZTExNDlmZGM1NTNlZjYzNjBmMjc5OWM2YyJ9fX0=",
                    "<green>Learn New Power",
                    "Fill all the slots on the right",
                    "with identical <blue>Power Stones",
                    "to permanently unlock their",
                    "<green>power<gray>.",
                    "",
                    "<yellow>Click to learn power!"
                    )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                sound(Sound.ENTITY_PLAYER_LEVELUP) {
                    volume = 2f
                    playAt(e.player.location)
                }
                player.memory.knownPowers.add(powerId!!)
                e.player.sendMessage(text("<yellow>You permanently unlocked the <green>${stones.first().name.str()}<yellow> power!"))
                e.player.openGUI(learnPowerUi(player, mutableListOf()))
            }
        } else {
            button(
                Slots.RowFourSlotThree, ItemValue.placeholderHeadDesc(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBjNDUyOGU2MjJiZDMxODcyMGQzOGUwZTQ1OTllNjliZjIzMzA4Zjg5NjkzOTIwZTBlNGVjYjU1ZDFjMGJhYyJ9fX0=",
                    "<red>Learn New Power",
                    "Fill all the slots on the right",
                    "with identical <blue>Power Stones",
                    "to permanently unlock their",
                    "<green>power<gray>.",
                    "",
                    if(stones.size != 9) "<red>Missing some stones!" else if(distinct.size != 1) "<red>Yikes! Random item detected!" else if(player.memory.knownPowers.contains(powerId)) "<red>Already learned!" else "<red>Higher combat level required!"
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                    pitch = 0f
                    playFor(e.player)
                }
                e.player.sendMessage(text("<red>Fill all slots in the grid with power stones to combine them!"))
            }
        }

        val compound = createCompound<Pair<Int, ItemStack>>({ it.second }) { e, (index, item) ->
            e.bukkitEvent.isCancelled = true
            e.player.giveOrDrop(item)
            stones.removeAt(index)
            e.player.openGUI(learnPowerUi(player, stones))
        }

        compoundSpace(Slots.RowThreeSlotFive rectTo Slots.RowFiveSlotSeven, compound)
        compound.addContent(stones.indices.map { it to stones[it].build(player)!! })
    }
}
