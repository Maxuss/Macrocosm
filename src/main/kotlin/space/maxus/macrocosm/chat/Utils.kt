package space.maxus.macrocosm.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

fun Component.noitalic() = decoration(TextDecoration.ITALIC, false)

fun String.reduceToList(preferredLength: Int = 25): List<String> {
    val list = mutableListOf<String>()
    // splitting words
    val words = split("\\s(?=\\S{2,})".toRegex())

    var curLen = 0
    val inter = StringBuilder()
    for (word in words) {
        curLen += word.length
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
