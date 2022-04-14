package space.maxus.macrocosm.stats

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.text.comp

@Suppress("unused")
enum class StatisticType(val color: TextColor) {
    OFFENSIVE(NamedTextColor.RED),
    DEFENSIVE(NamedTextColor.GREEN),
    SPECIAL(NamedTextColor.AQUA)

    ;

    fun format(num: Float): Component {
        val formatted = Formatting.withCommas(num.toBigDecimal(), false)
        return comp(formatted).color(color)
    }

    fun formatSigned(num: Float): Component? {
        val formatted = Formatting.withCommas(num.toBigDecimal(), false)
        val stringified = if(num < 0) "-$formatted" else if(num > 0) "+$formatted" else return null

        return comp(stringified).color(color)
    }
}
