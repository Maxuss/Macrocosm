package space.maxus.macrocosm.exceptions

import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.camelToSnakeCase
import java.net.URLEncoder

open class MacrocosmThrowable(val code: String, message: String = "Internal error.") : Throwable(message) {
    val component =
        text("<gray>($code): <red>${message.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} ").noitalic()

    // we usually don't need an embed, so delegate to lazy evaluation
    val embed by lazy {
        Discord.embed {
            setColor(Discord.COLOR_RED)
            setTitle("**Error!**")
            addField("**$code**", message, false)
        }
    }

    val reportUrl =
        "https://github.com/Maxuss/Macrocosm/issues/new?body=${
            URLEncoder.encode(
                "Automatic report: Unknown Error occurs: $message",
                Charsets.UTF_8
            )
        }&title=${URLEncoder.encode("Unexpected Error '$code'", Charsets.UTF_8)}&labels=bug"

}

val Throwable.macrocosm: MacrocosmThrowable
    get() {
        return if (this is MacrocosmThrowable) {
            this
        } else {
            MacrocosmThrowable(
                this.javaClass.simpleName.replaceFirstChar(Char::lowercase).camelToSnakeCase().uppercase(),
                this.message ?: this.cause?.message ?: "Unknown error! Please report this."
            )
        }
    }
