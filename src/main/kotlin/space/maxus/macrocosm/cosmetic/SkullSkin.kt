package space.maxus.macrocosm.cosmetic

import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Identifier

data class SkullSkin(val name: String, val rarity: Rarity, val skin: String, val target: Identifier, val isHelmet: Boolean = true): Cosmetic
