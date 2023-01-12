package space.maxus.macrocosm.chat

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Object, used for fancy formatting of numbers
 */
object Formatting {
    /**
     * Formats a [BigDecimal] to string with low amount of post-comma decimals
     *
     * @param num Number to be formatted
     * @param isInteger Whether to explicitly format the number with no decimals
     * @param scale Rounding scale of formatting
     * @return Formatted number string
     */
    fun stats(num: BigDecimal, isInteger: Boolean = false, scale: Int = 1): String {
        val format = if (isInteger || num.toDouble() % 1 == 0.0) DecimalFormat("#") else DecimalFormat("#0.${"0".repeat(scale)}")
        return format.format(num.setScale(scale, RoundingMode.DOWN).toDouble())
    }

    /**
     * Formats a [BigDecimal] to string with high amount of post-comma decimals
     *
     * @param num Number to be formatted
     * @param isInteger Whether to explicitly not include extra zeros
     * @param scale Rounding scale of formatting
     * @return Formatted number string
     */
    fun withCommas(num: BigDecimal, isInteger: Boolean = false, scale: Int = 1): String {
        val format = if (isInteger || num.toDouble() % 1 == 0.0) DecimalFormat("#,###") else DecimalFormat("#,##0.0")
        return format.format(num.setScale(scale, RoundingMode.DOWN).toDouble())
    }

    /**
     * Formats a [BigDecimal] to string without doing integer check
     *
     * @param num Number to be formatted
     * @return Formatted number string
     * @see Formatting.withCommas
     */
    fun withFullCommas(num: BigDecimal): String {
        return DecimalFormat("#,##0.0").format(num.setScale(1, RoundingMode.DOWN).toDouble())
    }
}
