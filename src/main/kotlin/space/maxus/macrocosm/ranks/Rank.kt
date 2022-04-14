package space.maxus.macrocosm.ranks

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.text.comp

@Suppress("unused")
enum class Rank(private val format: Component, val color: TextColor) {
    NONE(comp(""), NamedTextColor.GRAY),
    VIP(comp("<green>[VIP]"), NamedTextColor.GREEN),
    MVP(comp("<aqua>[MVP]"), NamedTextColor.AQUA),
    ADMIN(comp("<red>[ADMIN]"), NamedTextColor.RED)
    ;

    fun format(name: String, msg: String) = format.append(comp(" $name<white>: $msg"))
    fun id() = this.ordinal

    companion object {
        fun fromId(id: Int) = values()[id]
    }
}
