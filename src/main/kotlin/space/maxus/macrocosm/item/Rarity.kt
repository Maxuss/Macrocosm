package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.text.comp

@Suppress("UNUSED")
enum class Rarity(val color: TextColor, val canUpgrade: Boolean = true) {
    COMMON(NamedTextColor.WHITE),
    UNCOMMON(NamedTextColor.GREEN),
    RARE(NamedTextColor.BLUE),
    EPIC(NamedTextColor.DARK_PURPLE),
    LEGENDARY(NamedTextColor.GOLD),
    MYTHIC(NamedTextColor.LIGHT_PURPLE),
    GODLIKE(NamedTextColor.AQUA, false)
    ;

    fun format(upgraded: Boolean, ty: ItemType) = comp("<bold>")
        .color(color)
        .append(
            if (upgraded)
                comp("<obfuscated>a</obfuscated> $name ${if (ty == ItemType.OTHER) "" else "$ty "}<obfuscated>a</obfuscated>")
            else
                "$name${if (ty == ItemType.OTHER) "" else " $ty"}".toComponent()
        ).noitalic()

    fun next() = when (this) {
        COMMON -> UNCOMMON
        UNCOMMON -> RARE
        RARE -> EPIC
        EPIC -> LEGENDARY
        LEGENDARY -> MYTHIC
        MYTHIC -> GODLIKE
        GODLIKE -> COMMON
    }

    companion object {
        fun fromId(id: Int) = values()[id]
    }
}
