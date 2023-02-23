package space.maxus.macrocosm.ui

import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.general.id
import kotlin.random.Random

enum class UIs(val ui: MacrocosmUI) {
    TEST_UI_1(testUi1()),
    TEST_UI_2(MacrocosmUI.NullUi)
    ;

    companion object {
        fun init() {
            Registry.UI.delegateRegistration(values().map { id(it.name.lowercase()) to it.ui })
        }
    }
}

fun testUi1() = macrocosmUi("test_ui_1", UIDimensions.SIX_X_NINE) {
    title = "<red>Old title"

    background()

    switchUi(Slot.RowThreeSlotFive, testUi2("This is a test")) {
        ItemValue.placeholderDescripted(
            Material.ARROW,
            "Test <red>Button",
            "Does precisely",
            "<rainbow>nothing",
            "RNG: ${Random.nextInt()}"
        )
    }

    button(Slot.RowOneSlotOne, ItemValue.placeholder(Material.BLUE_BED, "<blue>Test")) { data ->
        data.animate {
            burn(
                Slot.RowOneSlotThree rect Slot.RowTwoSlotFour,
                dummy(Material.YELLOW_STAINED_GLASS_PANE),
                dummy(Material.ORANGE_STAINED_GLASS_PANE),
                dummy(Material.GRAY_STAINED_GLASS_PANE),
                3,
                3
            ) { it.sound(data.paper, Sound.UI_BUTTON_CLICK, pitch = 2f) }
        }
    }
}

fun testUi2(message: String) = macrocosmUi("test_ui_2", UIDimensions.SIX_X_NINE) {
    title = "<green>New title"

    background()

    val cmp = compound(
        Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight,
        1..80,
        { v -> ItemValue.placeholder(Material.values()[v], "V: $v") }
    ) { ui, v ->
        ui.player.sendMessage("Clicked $message $v")
    }

    compoundWidthScroll(Slot.RowOneSlotOne, cmp)
    compoundWidthScroll(Slot.RowOneSlotTwo, cmp, true)

    goBack(Slot.RowOneSlotFive)
}
