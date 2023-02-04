package space.maxus.macrocosm.npc.shop

import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoShopHistory

/**
 * Shop history data for a player
 */
data class ShopHistory(
    /**
     * Maximum amount of items that can be remembered in the history
     */
    val limit: Int,
    /**
     * The last sold items
     */
    val lastSold: MutableList<ItemStack>
): MongoConvert<MongoShopHistory> {
    override val mongo: MongoShopHistory
        get() = MongoShopHistory(limit, lastSold.map { it.serializeAsBytes() })
}
