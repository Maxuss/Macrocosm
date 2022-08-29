package space.maxus.macrocosm.bazaar

import com.google.gson.reflect.TypeToken
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.selectAll
import space.maxus.macrocosm.db.BazaarDataTable
import space.maxus.macrocosm.db.DataStorage
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

class BazaarTable private constructor(val itemData: ConcurrentHashMap<Identifier, BazaarItemData>): DatabaseStore {
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
                if(!outMap.containsKey(it)) {
                    outMap[it] = BazaarItemData.empty()
                }
            }
            return BazaarTable(outMap)
        }
    }

    val entries = itemData.size
    val ordersTotal = itemData.values.sumOf { entry -> entry.buy.size + entry.sell.size }

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
        data.transact {
            itemData.entries.parallelStream().forEach { (key, value) ->
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
            for(buy in cmp.buy.parallelStream()) {
                empty.buy.put(buy)
            }
            for(sell in cmp.sell.parallelStream()) {
                empty.sell.put(sell)
            }
            return empty
        }
    }

    val amount = buy.size + sell.size

    fun json() = toJson(BazaarOrderCompound(buy.toList(), sell.toList()))

    fun pushOrder(order: BazaarOrder) {
        if(order is BazaarBuyOrder)
            this.buy.offer(order)
        else if(order is BazaarSellOrder)
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
            if(specific is BazaarBuyOrder)
                this.buy.remove(specific)
            else if(specific is BazaarSellOrder)
                this.sell.remove(specific)
            NULL
        }
    }

    fun iterateBuy(iterator: FnArgRet<BazaarBuyOrder, Boolean>) {
        for(order in this.buy) {
            if(iterator(order))
                return
        }
    }

    fun iterateSell(iterator: FnArgRet<BazaarSellOrder, Boolean>) {
        for(order in this.sell) {
            if(iterator(order))
                return
        }
    }
}
