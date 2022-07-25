package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.text.text

@Suppress("UNUSED")
enum class Rarity(val color: TextColor, val canUpgrade: Boolean = true) {
    COMMON(NamedTextColor.WHITE),
    UNCOMMON(NamedTextColor.GREEN),
    RARE(NamedTextColor.BLUE),
    EPIC(NamedTextColor.DARK_PURPLE),
    LEGENDARY(NamedTextColor.GOLD),
    RELIC(TextColor.color(0xFFE7A8)),
    MYTHIC(NamedTextColor.LIGHT_PURPLE),
    DIVINE(NamedTextColor.AQUA, false),
    SPECIAL(NamedTextColor.RED, true),
    VERY_SPECIAL(TextColor.color(0xE45878)),
    UNOBTAINABLE(TextColor.color(0xA64CFD))
    ;

    fun format(upgraded: Boolean, ty: ItemType, dungeonised: Boolean = false) = text("<bold>")
        .color(color)
        .append(
            if (upgraded)
                text(
                    "<obfuscated>a</obfuscated> ${
                        name.replace(
                            "_",
                            " "
                        )
                    }${if (dungeonised) " DUNGEON " else ""}${if (ty == ItemType.OTHER) "" else "$ty "}<obfuscated>a</obfuscated>"
                )
            else
                "${
                    name.replace(
                        "_",
                        " "
                    )
                }${if (dungeonised) " DUNGEON " else " "}${if (ty == ItemType.OTHER) "" else ty.toString()}".toComponent()
        ).noitalic()

    fun next() = when (this) {
        COMMON -> UNCOMMON
        UNCOMMON -> RARE
        RARE -> EPIC
        EPIC -> LEGENDARY
        LEGENDARY -> RELIC
        RELIC -> MYTHIC
        MYTHIC -> DIVINE
        DIVINE -> SPECIAL
        SPECIAL -> VERY_SPECIAL
        VERY_SPECIAL -> COMMON
        UNOBTAINABLE -> UNOBTAINABLE
    }

    fun previous() = when (this) {
        COMMON -> DIVINE
        UNCOMMON -> COMMON
        RARE -> UNCOMMON
        EPIC -> RARE
        LEGENDARY -> EPIC
        RELIC -> LEGENDARY
        MYTHIC -> LEGENDARY
        DIVINE -> MYTHIC
        SPECIAL -> DIVINE
        VERY_SPECIAL -> SPECIAL
        UNOBTAINABLE -> UNOBTAINABLE
    }

    companion object {
        fun fromId(id: Int) = values()[id]
    }
}
