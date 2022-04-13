package space.maxus.macrocosm

import net.kyori.adventure.text.minimessage.MiniMessage

fun comp(str: String) = MiniMessage.miniMessage().deserialize(str)
