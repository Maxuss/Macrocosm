package space.maxus.macrocosm.npc.shop

import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoShopHistory

data class ShopHistory(
    val limit: Int,
    val lastSold: MutableList<ItemStack>
): MongoConvert<MongoShopHistory> {
    override val mongo: MongoShopHistory
        get() = MongoShopHistory(limit, lastSold.map { it.serializeAsBytes() })
}
