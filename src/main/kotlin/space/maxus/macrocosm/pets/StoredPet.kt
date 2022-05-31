package space.maxus.macrocosm.pets

import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Identifier

data class StoredPet(val id: Identifier, var rarity: Rarity, var level: Int, var overflow: Double, val skin: Identifier? = null)
