package space.maxus.macrocosm.ranks

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.text.text

@Suppress("unused")
enum class Rank(val format: Component, val color: TextColor) {
    NONE(text("<gray>"), NamedTextColor.GRAY),
    VIP(text("<green>[VIP] "), NamedTextColor.GREEN),
    MVP(text("<aqua>[MVP] "), NamedTextColor.AQUA),
    ADMIN(text("<red>[ADMIN] "), NamedTextColor.RED)
    ;

    fun format(name: String, msg: String) = format.append(text("$name<white>: $msg"))
    fun playerName(name: String) = format.append(text(name))
    fun id() = this.ordinal

    companion object {
        fun fromId(id: Int) = values()[id]
    }
}
