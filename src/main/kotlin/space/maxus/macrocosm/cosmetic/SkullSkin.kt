package space.maxus.macrocosm.cosmetic

import space.maxus.macrocosm.item.Rarity

data class SkullSkin(val name: String, val rarity: Rarity, val skin: String, val isHelmet: Boolean = true): Cosmetic
