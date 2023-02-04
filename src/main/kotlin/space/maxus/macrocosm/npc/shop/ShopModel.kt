package space.maxus.macrocosm.npc.shop

import space.maxus.macrocosm.registry.Identifier

/**
 * A model for shop data
 */
data class ShopModel(
    /**
     * Name of this shop
     */
    val name: String,
    /**
     * All items you can purchase in the shop
     */
    val items: List<Purchasable>
)

data class Purchasable(
    /**
     * ID of the item
     */
    val item: Identifier,
    /**
     * Price of the item in coins
     */
    val price: Number,
    /**
     * Amount of items that can be purchased
     */
    val amount: Int = 1,
    /**
     * Any other items required to purchase this item
     */
    val additionalItems: HashMap<Identifier, Int> = hashMapOf(),
    /**
     * Whether you can only purchase this item in the quantity specified in [amount]
     */
    val onlyOne: Boolean = false
)
