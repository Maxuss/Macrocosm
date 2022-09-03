package space.maxus.macrocosm.bazaar

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import space.maxus.macrocosm.database
import space.maxus.macrocosm.db.BazaarDataTable
import space.maxus.macrocosm.db.DataStorage
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.db.impl.AbstractSQLDatabase
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.*
import java.math.BigDecimal
import java.time.Duration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

data class BazaarOrderSummary(
    val amount: Int,
    val highestPrice: Double,
    val lowestPrice: Double,
    val averagePrice: Double,
    val medianPrice: Double,
    val cumulativeCoins: BigDecimal,
    val cumulativeItems: Int
)

data class BazaarItemSummary(
    val success: Boolean,
    val item: Identifier,
    val ordersCount: Int,
    val buyOrders: BazaarOrderSummary,
    val sellOrders: BazaarOrderSummary
)

class BazaarTable private constructor(val itemData: ConcurrentHashMap<Identifier, BazaarItemData>) : DatabaseStore {
    companion object {
        fun new() =
            BazaarTable(
                ConcurrentHashMap(
                    BazaarElement.allKeys.associateWithHashed { BazaarItemData.empty() }
                )
            )

        fun readSelf(data: DataStorage): BazaarTable {
            val outMap = ConcurrentHashMap<Identifier, BazaarItemData>()
            data.transact {
                BazaarDataTable.selectAll().forEach {
                    val orders = it[BazaarDataTable.orders]
                    outMap[Identifier.parse(it[BazaarDataTable.item])] = BazaarItemData.json(orders)
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

    val entries = itemData.size
    val ordersTotal = itemData.values.sumOf { entry -> entry.buy.size + entry.sell.size }
    private val summaryCache: Cache<Identifier, BazaarItemSummary> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()

    fun topBuyOrders(item: Identifier, amount: Int): Collection<BazaarBuyOrder> {
        return this.itemData[item]!!.buy.take(amount)
    }

    fun topSellOrders(item: Identifier, amount: Int): Collection<BazaarSellOrder> {
        return this.itemData[item]!!.sell.take(amount)
    }

    fun summary(item: Identifier): BazaarItemSummary? {
        val itemData = itemData[item] ?: return null
        val present = summaryCache.getIfPresent(item)
        if (present != null)
            return present
        val buyOrders = itemData.buy.toList()
        val sellOrders = itemData.sell.toList()
        val data = BazaarItemSummary(
            success = true,
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

    fun createOrder(order: BazaarOrder) {
        val data = itemData[order.item]!!
        data.pushOrder(order)
    }

    fun nextBuyOrder(item: Identifier): BazaarBuyOrder? {
        val data = itemData[item]!!
        return data.nextBuyOrder()
    }

    fun nextSellOrder(item: Identifier): BazaarSellOrder? {
        val data = itemData[item]!!
        return data.nextSellOrder()
    }

    fun iterateThroughOrdersBuy(item: Identifier, iterator: FnArgRet<BazaarBuyOrder, Boolean>) {
        val data = itemData[item]!!
        data.iterateBuy(iterator)
    }

    fun iterateThroughOrdersSell(item: Identifier, iterator: FnArgRet<BazaarSellOrder, Boolean>) {
        val data = itemData[item]!!
        data.iterateSell(iterator)
    }

    fun popOrder(order: BazaarOrder) {
        val data = itemData[order.item]!!
        data.popOrder(order)
    }

    override fun storeSelf(data: DataStorage) {
        transaction((database as AbstractSQLDatabase).connection) {
            itemData.entries.forEach { (key, value) ->
                BazaarDataTable.replace {
                    it[item] = key.toString()
                    it[orders] = GSON.toJson(value, object : TypeToken<BazaarItemData>() {}.type)
                }
            }
        }
    }
}

class BazaarItemData private constructor(
    val buy: BlockingQueue<BazaarBuyOrder>,
    val sell: BlockingQueue<BazaarSellOrder>
) {
    companion object {
        fun empty() = BazaarItemData(
            PriorityBlockingQueue(1) { a, b -> a.pricePer.compareTo(b.pricePer) },
            PriorityBlockingQueue(1) { a, b -> a.pricePer.compareTo(b.pricePer) },
        )

        fun json(json: String): BazaarItemData {
            val cmp: BazaarOrderCompound = fromJson(json)!!
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

    fun json() = toJson(BazaarOrderCompound(buy.toList(), sell.toList()))

    fun pushOrder(order: BazaarOrder) {
        if (order is BazaarBuyOrder)
            this.buy.offer(order)
        else if (order is BazaarSellOrder)
            this.sell.offer(order)
    }

    fun nextBuyOrder(): BazaarBuyOrder? {
        return this.buy.peek()
    }

    fun nextSellOrder(): BazaarSellOrder? {
        return this.sell.peek()
    }

    fun popOrder(specific: BazaarOrder) {
        runCatchingReporting {
            if (specific is BazaarBuyOrder)
                this.buy.remove(specific)
            else if (specific is BazaarSellOrder)
                this.sell.remove(specific)
            NULL
        }
    }

    fun iterateBuy(iterator: FnArgRet<BazaarBuyOrder, Boolean>) {
        for (order in this.buy) {
            if (iterator(order))
                return
        }
    }

    fun iterateSell(iterator: FnArgRet<BazaarSellOrder, Boolean>) {
        for (order in this.sell) {
            if (iterator(order))
                return
        }
    }
}
