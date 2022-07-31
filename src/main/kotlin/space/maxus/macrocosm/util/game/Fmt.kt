package space.maxus.macrocosm.util.game

import space.maxus.macrocosm.text.text

enum class Fmt(private val fmt: String) {
    SUPER_CRIT("<red>☠ Super Critical<gray>")
    ;

    val display = text(fmt)

    override fun toString(): String {
        return fmt
    }
}
