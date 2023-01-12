package space.maxus.macrocosm.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

private val MINIMESSAGE by lazy { MiniMessage.miniMessage() }

fun text(str: String) = MINIMESSAGE.deserialize(str)
fun Component.str() = MINIMESSAGE.serialize(this)
