package space.maxus.macrocosm.bazaar

import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.selectAll
import space.maxus.macrocosm.db.BazaarDataTable
import space.maxus.macrocosm.db.DataStorage
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

class BazaarTable private constructor(private val itemData: ConcurrentHashMap<Identifier, BazaarItemData>): DatabaseStore {
    companion object {
        fun new() =
            BazaarTable(
                ConcurrentHashMap(
                    Registry.BAZAAR_ELEMENTS
                        .iter().keys
                        .withAll(Registry.BAZAAR_ELEMENTS_REF.iter().keys)
                        .associateWithHashed { BazaarItemData.empty() }
                )
            )

        fun readSelf(data: DataStorage): BazaarTable {
            val outMap = ConcurrentHashMap<Identifier, BazaarItemData>()
            data.transact {
                BazaarDataTable.selectAll().forEach {
                    val orders = it[BazaarDataTable.orders]
                    val compound: BazaarDataCompound = fromJson(orders)!!
                    outMap[Identifier.parse(it[BazaarDataTable.item])] = BazaarItemData.from(compound.orders)
                }
            }
            Registry.BAZAAR_ELEMENTS_REF.iter().keys.withAll(Registry.BAZAAR_ELEMENTS.iter().keys).forEach {
                if(!outMap.containsKey(it)) {
                    outMap[it] = BazaarItemData.empty()
                }
            }
            return BazaarTable(outMap)
        }
    }

    fun createOrder(order: BazaarOrder) {
        val data = itemData[order.item]!!
        data.pushOrder(order)
    }

    fun nextOrder(item: Identifier): BazaarOrder? {
        val data = itemData[item]!!
        return data.nextOrder()
    }

    inline fun <reified O: BazaarOrder> nextOrderOfType(item: Identifier): O? {
        var next = nextOrder(item)
        while(next != null && next !is O) {
            next = nextOrder(item)
            if(next == null)
                return null
        }
        return next as? O
    }

    fun iterateThroughOrders(item: Identifier, iterator: FnArgRet<BazaarOrder, Boolean>) {
        val data = itemData[item]!!
        data.iterate(iterator)
    }

    fun popOrder(order: BazaarOrder) {
        val data = itemData[order.item]!!
        data.popOrder(order)
    }

    override fun storeSelf(data: DataStorage) {
        data.transact {
            for((key, value) in this@BazaarTable.itemData.entries) {
                BazaarDataTable.replace {
                    it[item] = key.toString()
                    it[orders] = value.json()
                }
            }
        }
    }
}

class BazaarItemData private constructor(private val orders: BlockingQueue<BazaarOrder>) {
    companion object {
        fun empty() = BazaarItemData(PriorityBlockingQueue(1) { a, b ->
            a.totalPrice.compareTo(b.totalPrice)
        })

        fun from(list: List<BazaarOrder>): BazaarItemData {
            val empty = empty()
            for(order in list) {
                empty.orders.put(order)
            }
            return empty
        }
        fun json(json: String) = BazaarItemData(fromJson(json)!!)
    }

    fun json() = toJson(BazaarDataCompound(orders.toList()))

    fun pushOrder(order: BazaarOrder) {
        this.orders.offer(order)
    }

    fun nextOrder(): BazaarOrder? {
        return this.orders.peek()
    }

    fun popOrder(specific: BazaarOrder) {
        runCatchingReporting {
            this.orders.remove(specific)
        }
    }

    fun iterate(iterator: FnArgRet<BazaarOrder, Boolean>) {
        for(order in this.orders) {
            if(iterator(order))
                return
        }
    }
}
