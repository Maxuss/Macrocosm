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
     * @return Formatted number string
     */
    fun stats(num: BigDecimal, isInteger: Boolean = false): String {
        val format = if (isInteger || num.toDouble() % 1 == 0.0) DecimalFormat("#") else DecimalFormat("#0.0")
        return format.format(num.setScale(1, RoundingMode.DOWN).toDouble())
    }

    /**
     * Formats a [BigDecimal] to string with high amount of post-comma decimals
     *
     * @param num Number to be formatted
     * @param isInteger Whether to explicitly not include extra zeros
     * @return Formatted number string
     */
    fun withCommas(num: BigDecimal, isInteger: Boolean = false): String {
        val format = if (isInteger || num.toDouble() % 1 == 0.0) DecimalFormat("#,###") else DecimalFormat("#,##0.0")
        return format.format(num.setScale(1, RoundingMode.DOWN).toDouble())
    }
}
