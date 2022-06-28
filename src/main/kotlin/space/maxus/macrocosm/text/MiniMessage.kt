package space.maxus.macrocosm.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun text(str: String) = MiniMessage.miniMessage().deserialize(str)
fun Component.str() = MiniMessage.miniMessage().serialize(this)
