package space.maxus.macrocosm.ui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.InventoryHolder

enum class UIDimensions(val width: Int, val height: Int) {
    ONE_X_NINE(9, 1),
    TWO_X_NINE(9, 2),
    THREE_X_NINE(9, 3),
    FOUR_X_NINE(9, 4),
    FIVE_X_NINE(9, 5),
    SIX_X_NINE(9, 6),

    ;

    val size = width * height

    fun bukkit(holder: InventoryHolder? = null, title: Component = Component.empty()) = Bukkit.createInventory(holder, size, title)

    companion object {
        fun fromRaw(size: Int): UIDimensions? {
            return when(size) {
                6 * 9 -> SIX_X_NINE
                5 * 9 -> FIVE_X_NINE
                4 * 9 -> FOUR_X_NINE
                3 * 9 -> THREE_X_NINE
                2 * 9 -> TWO_X_NINE
                1 * 9 -> ONE_X_NINE
                else -> null
            }
        }
    }
}
