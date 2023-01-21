package space.maxus.macrocosm.players.banking

import space.maxus.macrocosm.mongo.MongoDb
import space.maxus.macrocosm.util.drain
import space.maxus.macrocosm.util.runCatchingReporting
import java.util.concurrent.ConcurrentLinkedDeque

class TransactionHistory(private val transactions: ConcurrentLinkedDeque<Transaction>) {
    companion object {
        private const val memoryThreshold: Int = 2 shl 3
    }

    fun remember(transaction: Transaction) {
        if(transactions.size >= memoryThreshold)
            MongoDb.execute { it.transactions.insertMany(transactions.drain(memoryThreshold).map(Transaction::mongo)) }
        transactions.add(transaction)
    }

    fun storeSelf() {
        MongoDb.execute { db ->
            runCatchingReporting {
                if(transactions.isEmpty())
                    return@execute
                db.transactions.insertMany(transactions.drain(memoryThreshold).map(Transaction::mongo))
            }
        }
    }
}
