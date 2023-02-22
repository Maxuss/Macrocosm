package space.maxus.macrocosm.ui

import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.animation.CompositeAnimation
import space.maxus.macrocosm.ui.animation.UIRenderHelper
import space.maxus.macrocosm.ui.components.LinearComponentSpace
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import kotlin.random.Random

enum class UIs(val ui: MacrocosmUI) {
    TEST_1(testUi1()),
    TEST_2(testUi2())
    ;

    companion object {
        fun init() {
            Registry.UI.delegateRegistration(values().map { it.ui.id to it.ui })
        }
    }
}

fun testUi1() = macrocosmUi("test_ui_1", UIDimensions.SIX_X_NINE) {
    title = "<red>Old title"

    background()

    switchUi(Slot.RowThreeSlotFive, "test_ui_2") {
        ItemValue.placeholderDescripted(
            Material.ARROW,
            "Test <red>Button",
            "Does precisely",
            "<rainbow>nothing",
            "RNG: ${Random.nextInt()}"
        )
    }

    button(Slot.RowOneSlotOne, ItemValue.placeholder(Material.BLUE_BED, "<blue>Test")) {
        val anim = CompositeAnimation()
        anim.track(
            UIRenderHelper.burn(
                it.inventory,
                UIRenderHelper.dummy(Material.YELLOW_STAINED_GLASS_PANE),
                UIRenderHelper.dummy(Material.ORANGE_STAINED_GLASS_PANE),
                UIRenderHelper.dummy(Material.GRAY_STAINED_GLASS_PANE),
                LinearComponentSpace(
                    listOf(
                        Slot.RowOneSlotTwo,
                        Slot.RowOneSlotThree,
                        Slot.RowOneSlotFour,
                        Slot.RowOneSlotFive,
                        Slot.RowTwoSlotFive,
                        Slot.RowTwoSlotSix,
                        Slot.RowThreeSlotSix,
                        Slot.RowThreeSlotSeven,
                        Slot.RowThreeSlotEight,
                        Slot.RowFourSlotEight,
                        Slot.RowFourSlotNine,
                        Slot.RowFiveSlotNine,
                        Slot.RowSixSlotNine,
                        Slot.RowSixSlotEight
                    ).map(Slot::value)
                ),
                frequency = 3, delay = 3).sound(it.paper, Sound.UI_BUTTON_CLICK, pitch = 2f)
        )
        it.instance.renderAnimation(anim)
    }
}

fun testUi2() = macrocosmUi("test_ui_2", UIDimensions.SIX_X_NINE) {
    title = "<green>New title"

    background()
    val cmp = compound(Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight, 1..80, { v -> ItemValue.placeholder(Material.values()[v], "V: $v") }) { ui, v ->
        ui.player.sendMessage("Clicked $v")
    }
    compoundWidthScroll(Slot.RowOneSlotOne, cmp)
    compoundWidthScroll(Slot.RowOneSlotTwo, cmp, true)

    goBack(Slot.RowOneSlotFive)
}
