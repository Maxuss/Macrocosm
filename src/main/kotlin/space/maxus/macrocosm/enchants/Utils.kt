package space.maxus.macrocosm.enchants

import java.util.*

private val ROMAN_LOOKUP = TreeMap(
    hashMapOf(
        1000 to "M",
        900 to "CM",
        500 to "D",
        400 to "CD",
        100 to "C",
        50 to "L",
        40 to "XL",
        10 to "X",
        9 to "IX",
        5 to "V",
        4 to "IV",
        1 to "I"
    )
)

fun roman(num: Int): String {
    val l = ROMAN_LOOKUP.floorKey(num)
    if (num == l)
        return ROMAN_LOOKUP[l]!!
    return "${ROMAN_LOOKUP[l]!!}${roman(num - l)}"
}
