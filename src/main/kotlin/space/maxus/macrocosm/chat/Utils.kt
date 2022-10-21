package space.maxus.macrocosm.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

/**
 * A simple function to inline removing the italic formatting from the text component
 */
fun Component.noitalic() = decoration(TextDecoration.ITALIC, false)

/**
 * Attempts to sanely partition a string to list of strings, where each string's length is bounded by [preferredLength]
 *
 * Excludes the MiniMessage formatting tags (Regex: `<[^<>]+>`) from the counting process
 */
fun String.reduceToList(preferredLength: Int = 31): List<String> {
    val list = mutableListOf<String>()
    // splitting words
    val words = split("\\s(?=\\S{2,})".toRegex())

    var curLen = 0
    val inter = StringBuilder()
    for (word in words) {
        curLen += word.replace("<[^<>]+>".toRegex(), "").length
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

/**
 * Capitalizes a string, by first converting it to lower case and then converting to upper case the first character inside
 */
fun String.capitalized(separator: String = " ") =
    lowercase().split(" ").joinToString(separator) { str -> str.replaceFirstChar { it.titlecase(Locale.getDefault()) } }

