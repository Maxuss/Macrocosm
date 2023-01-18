package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.stripTags

data class LinearInventorySlots<T : ForInventory>(val slots: List<InventorySlot>) : InventorySlotCompound<T> {
    override fun withInvType(invType: GUIType<T>): Collection<InventorySlot> {
        return slots
    }
}

fun IntRange.slots(row: Int): Array<InventorySlot> {
    return map { InventorySlot(row, it) }.toTypedArray()
}

fun slayerChooseMenu(player: MacrocosmPlayer): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    title = text("Slayer Menu")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        // specific slayer buttons
        val cmp = createRectCompound<Int>(Slots.RowThreeSlotThree, Slots.RowThreeSlotSeven, iconGenerator = { ty ->
            if (ty == -1) {
                return@createRectCompound ItemValue.placeholderDescripted(
                    Material.COAL_BLOCK,
                    "<yellow>Coming Soon",
                    "<red>In development!"
                )
            }
            val it = SlayerType.values()[ty]
            if (it.slayer.requirementCheck(player))
                ItemValue.placeholderDescripted(
                    it.slayer.item,
                    "<red>\uD83D\uDC80 <yellow>${it.slayer.name.stripTags()}",
                    *it.slayer.description.reduceToList(20).toMutableList()
                        .apply {
                            add(""); add("<gray>${it.slayer.entityKind} Slayer: <yellow>LVL ${player.slayers[it]!!.level + 1}"); add(
                            ""
                        ); add("<yellow>Click to view boss.")
                        }
                        .toTypedArray()
                )
            else
                ItemValue.placeholderDescripted(
                    it.slayer.item,
                    "<red>\uD83D\uDC80 <yellow>${it.slayer.name.stripTags()}",
                    *it.slayer.description.reduceToList(20).toMutableList()
                        .apply { add(""); add(it.slayer.requirementString); add(""); }
                        .toTypedArray()
                )
        }, onClick = { e, it ->
            e.bukkitEvent.isCancelled = true
            if (it == -1)
                return@createRectCompound
            val ty = SlayerType.values()[it]
            if (!ty.slayer.requirementCheck(player)) {
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
        cmp.addContent(SlayerType.values().map { it.ordinal }.padForward(5, -1))

        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close"), onClick = {
            it.bukkitEvent.isCancelled = true
            it.player.closeInventory()
        })
    }
}


inline fun confirmationRedirect(crossinline then: (MacrocosmPlayer) -> Unit) = kSpigotGUI(GUIType.THREE_BY_NINE) {
    title = text("Confirmation")
    defaultPage = 0
    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(Slots.RowTwoSlotFour, ItemValue.placeholder(Material.GREEN_CONCRETE, "<green>Confirm"), onClick = {
            it.bukkitEvent.isCancelled = true
            then(it.player.macrocosm!!)
        })
        button(Slots.RowTwoSlotSix, ItemValue.placeholder(Material.RED_CONCRETE, "<red>Cancel"), onClick = {
            it.bukkitEvent.isCancelled = true
            it.player.closeInventory()
        })
    }
}
