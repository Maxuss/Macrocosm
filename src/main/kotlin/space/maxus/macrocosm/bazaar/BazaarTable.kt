package space.maxus.macrocosm.bazaar

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import space.maxus.macrocosm.database
import space.maxus.macrocosm.db.BazaarDataTable
import space.maxus.macrocosm.db.DataStorage
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.db.impl.AbstractSQLDatabase
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.serde.Bytes
import space.maxus.macrocosm.util.*
import java.math.BigDecimal
import java.time.Duration
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

/**
 * Summary of a sequence of bazaar orders
 */
data class BazaarOrderSummary(
    /**
     * Total amount of items in the orders
     */
    val amount: Int,
    /**
     * Highest per-item price among all the orders
     */
    val highestPrice: Double,
    /**
     * Lowest per-item price among all the orders
     */
    val lowestPrice: Double,
    /**
     * Average per-item price
     */
    val averagePrice: Double,
    /**
     * Median per-item price
     */
    val medianPrice: Double,
    /**
     * Cumulative amount of coins in all the orders
     */
    val cumulativeCoins: BigDecimal,
    /**
     * Cumulative amount of items in all the orders
     */
    val cumulativeItems: Int
)

/**
 * Summary for a single bazaar item
 */
data class BazaarItemSummary(
    /**
     * Item this summary belongs to
     */
    val item: Identifier,
    /**
     * Total amount of buy and sell orders for this item
     */
    val ordersCount: Int,
    /**
     * Summary of buy orders
     */
    val buyOrders: BazaarOrderSummary,
    /**
     * Summary of sell orders
     */
    val sellOrders: BazaarOrderSummary
)

/**
 * LOB-like bazaar order table
 */
class BazaarTable private constructor(val itemData: ConcurrentHashMap<Identifier, BazaarItemData>) : DatabaseStore {
    companion object {
        /**
         * Constructs new empty bazaar table
         */
        fun new() =
            BazaarTable(
                ConcurrentHashMap(
                    BazaarElement.allKeys.associateWithHashed { BazaarItemData.empty() }
                )
            )

        /**
         * Reads itself from the provided [DataStorage]
         */
        fun readSelf(data: DataStorage): BazaarTable {
            val outMap = ConcurrentHashMap<Identifier, BazaarItemData>()
            data.transact {
                BazaarDataTable.selectAll().forEach {
                    val orders = it[BazaarDataTable.orders]
                    outMap[Identifier.parse(it[BazaarDataTable.item])] = BazaarItemData.deserialize(orders)
                }
            }
            BazaarElement.allKeys.forEach {
                if (!outMap.containsKey(it)) {
                    outMap[it] = BazaarItemData.empty()
                }
            }
            return BazaarTable(outMap)
        }
    }

    /**
     * Total count of item entries. This is equal to the total amount of values of the [BazaarElement] enum
     */
    val entries = itemData.size

    /**
     * Total amount of both buy and sell orders
     */
    val ordersTotal = itemData.values.sumOf { entry -> entry.buy.size + entry.sell.size }
    private val summaryCache: Cache<Identifier, BazaarItemSummary> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()

    /**
     * Gets the [amount] top buy orders, sorted by the price per item descending
     *
     * @param item item to be queried
     * @param amount amount of items to be queried
     */
    fun topBuyOrders(item: Identifier, amount: Int): Collection<BazaarBuyOrder> {
        return this.itemData[item]?.buy?.take(amount) ?: Collections.emptySet()
    }

    /**
     * Gets the [amount] top sell orders, sorted by the price per item ascending
     *
     * @param item item to be queried
     * @param amount amount of items to be queried
     */
    fun topSellOrders(item: Identifier, amount: Int): Collection<BazaarSellOrder> {
        return this.itemData[item]?.sell?.take(amount) ?: Collections.emptySet()
    }

    /**
     * Attempts to count summary of a bazaar item.
     * This is a pretty expensive operation because it uses kotlin-provided sorting method ([kotlin.collections.sorted]) under the hood.
     *
     * @param item Item to be queried
     */
    fun summary(item: Identifier): BazaarItemSummary? {
        val itemData = itemData[item] ?: return null
        val present = summaryCache.getIfPresent(item)
        if (present != null)
            return present
        val buyOrders = itemData.buy.toList()
        val sellOrders = itemData.sell.toList()
        val data = BazaarItemSummary(
            item = item,
            ordersCount = buyOrders.size + sellOrders.size,
            buyOrders = BazaarOrderSummary(
                amount = buyOrders.size,
                highestPrice = buyOrders.maxByOrNull { it.pricePer }?.pricePer ?: .0,
                lowestPrice = buyOrders.minByOrNull { it.pricePer }?.pricePer ?: .0,
                averagePrice = buyOrders.map { it.pricePer }.average() insteadOfNaN .0,
                medianPrice = buyOrders.map { it.pricePer }.median(),
                cumulativeCoins = buyOrders.sumOf { it.totalPrice },
                cumulativeItems = buyOrders.sumOf { it.qty }
            ),
            sellOrders = BazaarOrderSummary(
                amount = sellOrders.size,
                highestPrice = sellOrders.maxByOrNull { it.pricePer }?.pricePer ?: .0,
                lowestPrice = sellOrders.minByOrNull { it.pricePer }?.pricePer ?: .0,
                averagePrice = sellOrders.map { it.pricePer }.average() insteadOfNaN .0,
                medianPrice = sellOrders.map { it.pricePer }.median(),
                cumulativeCoins = sellOrders.sumOf { it.totalPrice },
                cumulativeItems = sellOrders.sumOf { it.qty }
            )
        )
        // caching due to expensive operations used
        summaryCache.put(item, data)
        return data
    }

    /**
     * Creates a new bazaar order
     */
    fun createOrder(order: BazaarOrder) {
        val data = itemData[order.item]!!
        data.pushOrder(order)
    }

    /**
     * Gets the next buy order without shifting the queue
     */
    fun nextBuyOrder(item: Identifier): BazaarBuyOrder? {
        val data = itemData[item]!!
        return data.nextBuyOrder()
    }

    /**
     * Gets the next sell order without shifting the queue
     */
    fun nextSellOrder(item: Identifier): BazaarSellOrder? {
        val data = itemData[item]!!
        return data.nextSellOrder()
    }

    /**
     * Iterates through the buy orders
     *
     * @param item item to be queried
     * @param iterator iterator function to be applied, takes in an order and returns true if the iteration should continue and false if it should be stopped
     */
    fun iterateThroughOrdersBuy(item: Identifier, iterator: FnArgRet<BazaarBuyOrder, Boolean>) {
        val data = itemData[item]!!
        data.iterateBuy(iterator)
    }

    /**
     * Iterates through the sell orders
     *
     * @param item item to be queried
     * @param iterator iterator function to be applied, takes in an order and returns true if the iteration should continue and false if it should be stopped
     */
    fun iterateThroughOrdersSell(item: Identifier, iterator: FnArgRet<BazaarSellOrder, Boolean>) {
        val data = itemData[item]!!
        data.iterateSell(iterator)
    }

    /**
     * Pops the provided order from the queue
     */
    fun popOrder(order: BazaarOrder) {
        val data = itemData[order.item]!!
        data.popOrder(order)
    }

    /**
     * Saves itself at the provided [DataStorage]
     */
    override fun storeSelf(data: DataStorage) {
        transaction((database as AbstractSQLDatabase).connection) {
            itemData.entries.forEach { (key, value) ->
                BazaarDataTable.replace {
                    it[item] = key.toString()
                    it[orders] = value.serialize()
                }
            }
        }
    }
}

/**
 * Bazaar data for a single item
 */
class BazaarItemData private constructor(
    /**
     * All the buy orders in a priority queue
     */
    val buy: BlockingQueue<BazaarBuyOrder>,
    /**
     * All the sell orders in a priority queue
     */
    val sell: BlockingQueue<BazaarSellOrder>
) {
    companion object {
        /**
         * Constructs an empty bazaar data
         */
        fun empty() = BazaarItemData(
            PriorityBlockingQueue(1) { a, b -> a.pricePer.compareTo(b.pricePer) },
            PriorityBlockingQueue(1) { a, b -> b.pricePer.compareTo(a.pricePer) },
        )

        /**
         * Deserializes itself from a JSON string
         *
         * note: this is a rather unoptimized method, more compact approach is possible (see [this issue](https://github.com/Maxuss/Macrocosm/issues/3))
         */
        fun deserialize(data: String): BazaarItemData {
            val cmp: BazaarOrderCompound = Bytes.deserialize(data).obj()
            val empty = empty()
            for (buy in cmp.buy.parallelStream()) {
                empty.buy.put(buy)
            }
            for (sell in cmp.sell.parallelStream()) {
                empty.sell.put(sell)
            }
            return empty
        }
    }

    val amount = buy.size + sell.size

    /**
     * Saves itself to a JSON string
     *
     * note: this is a rather unoptimized method, more compact approach is possible (see [this issue](https://github.com/Maxuss/Macrocosm/issues/3))
     */
    fun serialize(): String {
        return Bytes.serialize().obj(BazaarOrderCompound(buy.toList(), sell.toList())).end()
    }

    /**
     * Pushes an order to this queue
     */
    fun pushOrder(order: BazaarOrder) {
        if (order is BazaarBuyOrder)
            this.buy.offer(order)
        else if (order is BazaarSellOrder)
            this.sell.offer(order)
    }

    /**
     * Gets the next buy order without shifting the queue
     */
    fun nextBuyOrder(): BazaarBuyOrder? {
        return this.buy.peek()
    }

    /**
     * Gets the next sell order without shifting the queue
     */
    fun nextSellOrder(): BazaarSellOrder? {
        return this.sell.peek()
    }

    /**
     * Pops an order from the queue
     */
    fun popOrder(specific: BazaarOrder) {
        runCatchingReporting {
            if (specific is BazaarBuyOrder)
                this.buy.remove(specific)
            else if (specific is BazaarSellOrder)
                this.sell.remove(specific)
            NULL
        }
    }

    /**
     * Iterates through buy orders
     */
    fun iterateBuy(iterator: FnArgRet<BazaarBuyOrder, Boolean>) {
        for (order in this.buy) {
            if (iterator(order))
                return
        }
    }

    /**
     * Iterates through sell orders
     */
    fun iterateSell(iterator: FnArgRet<BazaarSellOrder, Boolean>) {
        for (order in this.sell) {
            if (iterator(order))
                return
        }
    }
}
