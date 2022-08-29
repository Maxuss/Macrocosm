package space.maxus.macrocosm.exceptions

import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.camelToSnakeCase

@Suppress("CanBeParameter")
open class MacrocosmThrowable(val code: String, message: String = "Internal error.") : Throwable(message) {
    val component =
        text("<gray>($code): <red>${message.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} ").noitalic()
}

val Throwable.macrocosm: MacrocosmThrowable
    get() {
        return if (this is MacrocosmThrowable) {
            this
        } else {
            MacrocosmThrowable(
                this.javaClass.simpleName.replaceFirstChar(Char::lowercase).camelToSnakeCase().uppercase(),
                this.message ?: "Unknown error! Please report this."
            )
        }
    }
