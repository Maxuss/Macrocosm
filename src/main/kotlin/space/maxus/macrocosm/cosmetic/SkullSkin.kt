package space.maxus.macrocosm.cosmetic

import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Identifier

/**
 * A cosmetic which applies custom skin to a skull item
 */
data class SkullSkin(
    /**
     * Display item of the skin
     */
    val name: String,
    /**
     * Rarity of the skin item
     */
    val rarity: Rarity,
    /**
     * Skin Base64 data, see [Minecraft Heads website](https://minecraft-heads.com)
     */
    val skin: String,
    /**
     * ID of the item this skin applies to
     */
    val target: Identifier,
    /**
     * Whether this skull applies to helmet. If false only applies to pets instead
     */
    val isHelmet: Boolean = true
) : Cosmetic
