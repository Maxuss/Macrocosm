package space.maxus.macrocosm.mongo.data

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import space.maxus.macrocosm.bazaar.BazaarBuyOrder
import space.maxus.macrocosm.bazaar.BazaarItemData
import space.maxus.macrocosm.bazaar.BazaarOrder
import space.maxus.macrocosm.bazaar.BazaarSellOrder
import space.maxus.macrocosm.mongo.MongoRepr
import space.maxus.macrocosm.registry.Identifier
import java.util.*
import java.util.concurrent.PriorityBlockingQueue

data class MongoBazaarOrder(
    val kind: MongoBazaarOrderKind,
    val item: String,
    val createdBy: UUID,
    val originalAmount: Int,
    val createdAt: Long,
    val quantity: Int,
    val pricePer: Double,
    val boughtOrSold: Int,
    val buyersOrSellers: MutableList<UUID>,
    @BsonId
    val key: Id<MongoBazaarOrder> = newId(),
) : MongoRepr<BazaarOrder> {
    @JsonIgnore
    override val actual: BazaarOrder = if (kind == MongoBazaarOrderKind.BUY) {
        BazaarBuyOrder(
            Identifier.parse(item),
            quantity,
            pricePer,
            boughtOrSold,
            buyersOrSellers,
            createdBy,
            originalAmount,
            createdAt
        )
    } else {
        BazaarSellOrder(
            Identifier.parse(item),
            quantity,
            pricePer,
            boughtOrSold,
            buyersOrSellers,
            createdBy,
            originalAmount,
            createdAt
        )
    }
}

enum class MongoBazaarOrderKind {
    BUY,
    SELL
}

data class MongoBazaarData(
    @BsonId
    val item: String,
    val buy: List<MongoBazaarOrder>,
    val sell: List<MongoBazaarOrder>
) : MongoRepr<BazaarItemData> {
    override val actual: BazaarItemData
        @JsonIgnore
        get() = BazaarItemData(
            PriorityBlockingQueue(buy.map { it.actual as BazaarBuyOrder }),
            PriorityBlockingQueue(sell.map { it.actual as BazaarSellOrder })
        )
}
