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
    GODLIKE(TextColor.color(0xC96B3D), false)
    ;

    fun format(upgraded: Boolean) = comp("<bold>")
        .color(color)
        .append(
            if(upgraded)
                comp("<obfuscated>a</obfuscated> $name <obfuscated>a</obfuscated>")
            else
                name.toComponent()
        ).noitalic()
}
