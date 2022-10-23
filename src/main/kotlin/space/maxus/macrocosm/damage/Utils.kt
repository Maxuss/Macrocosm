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

/**
 * Clamps the value between the minimum and maximum
 */
fun clamp(value: Float, min: Float, max: Float) = max(min, min(max, value))

/**
 * Truncates a big number to a pretty string
 *
 * Formula:
 * * -> ${NUM / 1_000_000}M if NUM >= 1_000_000
 * * -> ${NUM / 1_000}k if NUM >= 100_000
 * * -> ${NUM} else
 *
 * @param number number to be truncated
 * @param applyFormatToInt whether to format the integers
 *
 * @return fancy truncated number string
 */
fun truncateBigNumber(number: Float, applyFormatToInt: Boolean = false): String {
    return if (number >= 1_000_000) {
        // > 1M
        "${DecimalFormat("#.#").format(number / 1_000_000f)}M"
    } else if (number >= 100_000) {
        // > 100k
        "${(number / 1000f).roundToInt()}k"
    } else if (applyFormatToInt) Formatting.stats(number.toBigDecimal(), true) else number.roundToInt().toString()
}

/**
 * Determines the color of name tag of entity depending on it's maximum health
 *
 * @param amount current health
 * @param max maximum health
 *
 * @return color to be used for formatting
 */
fun healthColor(amount: Float, max: Float): TextColor {
    val ratio = amount / max
    return if (ratio > 0.5f)
        NamedTextColor.GREEN
    else
        NamedTextColor.YELLOW
}

/**
 * Calculates a location relative to the provided location with this vector
 *
 * @param location location to be used
 *
 * @return location relative to the provided [location]
 */
fun Vector.relativeLocation(location: Location) =
    Location(location.world, location.x + x, location.y + y, location.z + z)
