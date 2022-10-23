package space.maxus.macrocosm.stats

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.text.text
import java.io.Serializable

@Suppress("unused")
enum class StatisticType(val color: TextColor) : Serializable {
    OFFENSIVE(NamedTextColor.RED),
    DEFENSIVE(NamedTextColor.GREEN),
    SPECIAL(NamedTextColor.AQUA)

    ;

    fun format(num: Float): Component {
        val formatted = Formatting.stats(num.toBigDecimal(), false)
        return text(formatted)
    }

    fun formatSigned(num: Float, colored: Boolean = true): Component? {
        val formatted = Formatting.stats(num.toBigDecimal(), false)
        val stringified = if (num < 0) formatted else if (num > 0) "+$formatted" else return null

        return if (colored) text(stringified).color(color) else text(stringified)
    }
}
