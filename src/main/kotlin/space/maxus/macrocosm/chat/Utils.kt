package space.maxus.macrocosm.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

fun Component.noitalic() = decoration(TextDecoration.ITALIC, false)
