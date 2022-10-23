package space.maxus.macrocosm.cosmetic

import org.bukkit.Material
import space.maxus.macrocosm.item.Rarity

/**
 * A cosmetic that applies a dye to a leather armor piece
 */
data class Dye(
    /**
     * Display name of the dye
     */
    val name: String,
    /**
     * Color the dye applies
     */
    val color: Int,
    /**
     * Rarity of the dye item
     */
    val rarity: Rarity,
    /**
     * The item this dye is represented by, e.g. [Material.INK_SAC]
     */
    val repr: Material,
    /**
     * A special character this dye adds to applied item's name
     */
    val specialChar: String = "\uD83C\uDF38"
) : Cosmetic
