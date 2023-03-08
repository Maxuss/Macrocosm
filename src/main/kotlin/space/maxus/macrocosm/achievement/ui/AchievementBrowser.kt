package space.maxus.macrocosm.achievement.ui

import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi

fun achievementBrowser(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("achievement_browser", UIDimensions.FIVE_X_NINE) {
    title = "Achievement Browser"

    page {
        background()
        close()

        val cmp = compound(Slot.RowTwoSlotTwo rect Slot.RowFourSlotEight, Registry.ACHIEVEMENT.iter().values.sortedBy { it.rarity.ordinal }, { ach ->
            ach.buildItem(player)
        }) { _, _ -> }

        compoundWidthScroll(Slot.RowFiveSlotEight, cmp, reverse = true)
        compoundWidthScroll(Slot.RowFiveSlotNine, cmp)
    }
}
