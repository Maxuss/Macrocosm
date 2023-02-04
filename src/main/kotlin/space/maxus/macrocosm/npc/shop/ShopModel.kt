package space.maxus.macrocosm.npc.shop

import space.maxus.macrocosm.registry.Identifier

data class ShopModel(
    val name: String,
    val items: List<Purchasable>
)

data class Purchasable(
    val item: Identifier,
    val price: Number,
    val amount: Int = 1,
    val additionalItems: HashMap<Identifier, Int> = hashMapOf(),
    val onlyOne: Boolean = false
)
