package space.maxus.macrocosm.accessory.ui

import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi

fun powerStonesGuide(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("power_stone_guide", UIDimensions.SIX_X_NINE) {
    title = "Power Stones Guide"

    page {
        background()

        goBack(Slot.RowSixSlotFive, learnPowerUi(player, mutableListOf()))

        compound(Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight,
            Registry.ACCESSORY_POWER.iter().values.filterIsInstance<StoneAccessoryPower>()
                .sortedByDescending { it.combatLevel }, { it.guideItem(player) }) { _, _ -> }
    }
}
