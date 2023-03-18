
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.RectComponentSpace
import space.maxus.macrocosm.ui.components.Slot

fun main() {
    val rect = RectComponentSpace(
        Slot.RowTwoSlotTwo,
        Slot.RowFiveSlotEight,
    )
    for(slot in rect.enumerate(UIDimensions.SIX_X_NINE)) {
        println(slot)
    }
}
