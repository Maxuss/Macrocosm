package space.maxus.macrocosm.cosmetic

import org.bukkit.Material
import space.maxus.macrocosm.item.Rarity

data class Dye(
    val name: String,
    val color: Int,
    val rarity: Rarity,
    val repr: Material,
    val specialChar: String = "☆"
) : Cosmetic
