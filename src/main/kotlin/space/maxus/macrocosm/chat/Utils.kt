package space.maxus.macrocosm.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

fun Component.noitalic() = decoration(TextDecoration.ITALIC, false)

fun String.mmLength() = replace("<[^<>]+>".toRegex(), "").length

fun String.reduceToList(preferredLength: Int = 31): List<String> {
    val list = mutableListOf<String>()
    // splitting words
    val words = split("\\s(?=\\S{2,})".toRegex())

    var curLen = 0
    val inter = StringBuilder()
    for (word in words) {
        curLen += word.mmLength()
        inter.append(word)
        if (curLen >= preferredLength) {
            curLen = 0
            list.add(inter.toString())
            inter.clear()
            continue
        }
        inter.append(' ')
    }

    list.add(inter.toString())

    return list
}

fun String.capitalized(separator: String = " ") =
    lowercase().split(" ").joinToString(separator) { str -> str.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

fun String.isBlankOrEmpty() = isBlank() || isEmpty()
