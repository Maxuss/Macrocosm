package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.stripTags

fun IntRange.slots(row: Int): Array<Slot> {
    return map { Slot(row, it) }.toTypedArray()
}

fun slayerChooseMenu(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("slayer_choose", UIDimensions.FOUR_X_NINE) {
    title = "Slayer Menu"

    page {
        background()

        // specific slayer buttons
        compound(
            Slot.RowTwoSlotThree rect Slot.RowTwoSlotSeven,
            SlayerType.values().map { it.ordinal }.padForward(5, -1),
            { ty ->
            if (ty == -1) {
                return@compound ItemValue.placeholderDescripted(
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
        }, { e, it ->
            if (it == -1)
                return@compound
            val ty = SlayerType.values()[it]
            if (!ty.slayer.requirementCheck(player)) {
                player.sendMessage("<red>You do not meet requirements to start this slayer quest!")
                player.sendMessage("${ty.slayer.name} ${ty.slayer.requirementString}")
                e.paper.closeInventory()
                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                    pitch = 0f
                    playFor(e.paper)
                }
            } else {
                e.instance.switch(specificSlayerMenu(player, ty))
            }
        })

        close()
    }
}


inline fun confirmationRedirect(crossinline then: (MacrocosmPlayer) -> Unit): MacrocosmUI = macrocosmUi("confirmation", UIDimensions.THREE_X_NINE) {
    title = "Confirmation"
    page(0) {
        background()

        button(Slot.RowTwoSlotFour, ItemValue.placeholder(Material.GREEN_CONCRETE, "<green>Confirm")) {
            then(it.player)
        }
        button(Slot.RowTwoSlotSix, ItemValue.placeholder(Material.RED_CONCRETE, "<red>Cancel")) {
            it.paper.closeInventory()
        }
    }
}
