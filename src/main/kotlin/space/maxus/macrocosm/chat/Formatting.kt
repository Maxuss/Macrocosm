package space.maxus.macrocosm.chat

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

object Formatting {
    fun withCommas(num: BigDecimal, isInteger: Boolean = false): String {
        val format = if(isInteger || num.toDouble() % 1 == 0.0) DecimalFormat("#,###") else DecimalFormat("#,##0.0")
        return format.format(num.setScale(1, RoundingMode.DOWN).toDouble())
    }
}
