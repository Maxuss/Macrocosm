package space.maxus.macrocosm.damage

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.util.Vector
import space.maxus.macrocosm.chat.Formatting
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun clamp(value: Float, min: Float, max: Float) = max(min, min(max, value))

fun truncateBigNumber(number: Float, applyFormatToInt: Boolean = false): String {
    return if (number >= 1_000_000) {
        // > 1M
        "${DecimalFormat("#.#").format(number / 1_000_000f)}M"
    } else if (number >= 100_000) {
        // > 100k
        "${(number / 1000f).roundToInt()}k"
    } else if (applyFormatToInt) Formatting.stats(number.toBigDecimal(), true) else number.roundToInt().toString()
}

fun healthColor(amount: Float, max: Float): TextColor {
    val ratio = amount / max
    return if (ratio > 0.5f)
        NamedTextColor.GREEN
    else
        NamedTextColor.YELLOW
}

fun Vector.relativeLocation(location: Location) =
    Location(location.world, location.x + x, location.y + y, location.z + z)
