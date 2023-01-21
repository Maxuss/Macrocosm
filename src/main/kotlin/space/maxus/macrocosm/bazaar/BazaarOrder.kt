@file:Suppress("PropertyName")

package space.maxus.macrocosm.bazaar

import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoBazaarOrder
import space.maxus.macrocosm.mongo.data.MongoBazaarOrderKind
import space.maxus.macrocosm.registry.Identifier
import java.io.Serializable
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * A compound used for JSON serialization of [bazaar item data][BazaarTable]
 */
data class BazaarOrderCompound(val buy: List<BazaarBuyOrder>, val sell: List<BazaarSellOrder>) : Serializable

/**
 * A singular bazaar order
 */
abstract class BazaarOrder(
    /**
     * Item this order belongs to
     */
    val item: Identifier,
    /**
     * By whom this order was created
     */
    val createdBy: UUID,
    /**
     * Original amount ordered
     */
    val originalAmount: Int,
    /**
     * Epoch milliseconds when this order was created
     */
    val createdAt: Long = Instant.now().toEpochMilli()
) : Serializable {
    /**
     * Total coins accumulated in this order
     */
    abstract val totalPrice: BigDecimal
}

/**
 * A bazaar BUY order
 */
class BazaarBuyOrder(
    item: Identifier,
    /**
     * Quantity of items ordered to buy
     */
    var qty: Int,
    /**
     * Price per single item
     */
    val pricePer: Double,
    /**
     * Amount of items already bought
     */
    var bought: Int,
    /**
     * UUIDs of players who sold items to this order
     */
    val sellers: MutableList<UUID>,
    createdBy: UUID,
    originalAmount: Int,
    createdAt: Long = Instant.now().toEpochMilli()
) : BazaarOrder(item, createdBy, originalAmount, createdAt), Serializable, MongoConvert<MongoBazaarOrder>,
    Comparable<BazaarBuyOrder> {
    override val totalPrice: BigDecimal = pricePer.toBigDecimal() * qty.toBigDecimal()
    override val mongo: MongoBazaarOrder
        get() = MongoBazaarOrder(
            MongoBazaarOrderKind.BUY,
            item.toString(),
            createdBy,
            originalAmount,
            createdAt,
            qty,
            pricePer,
            bought,
            sellers
        )

    override fun compareTo(other: BazaarBuyOrder): Int {
        return pricePer.compareTo(other.pricePer)
    }
}

/**
 * A bazaar SELL order
 */
class BazaarSellOrder(
    item: Identifier,
    /**
     * Quantity of items ordered to sell
     */
    var qty: Int,
    /**
     * Price per single item
     */
    val pricePer: Double,
    /**
     * Amount of items already sold
     */
    var sold: Int,
    /**
     * UUIDs of players who bought items off this order
     */
    val buyers: MutableList<UUID>,
    createdBy: UUID,
    originalAmount: Int,
    createdAt: Long = Instant.now().toEpochMilli()
) : BazaarOrder(item, createdBy, originalAmount, createdAt), Serializable, MongoConvert<MongoBazaarOrder>,
    Comparable<BazaarSellOrder> {
    override val totalPrice: BigDecimal = pricePer.toBigDecimal() * qty.toBigDecimal()

    override val mongo: MongoBazaarOrder
        get() = MongoBazaarOrder(
            MongoBazaarOrderKind.SELL,
            item.toString(),
            createdBy,
            originalAmount,
            createdAt,
            qty,
            pricePer,
            sold,
            buyers
        )

    override fun compareTo(other: BazaarSellOrder): Int {
        return other.pricePer.compareTo(pricePer)
    }
}
