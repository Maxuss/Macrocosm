package space.maxus.macrocosm.players.banking

import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoTransaction
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class Transaction(val player: UUID, val kind: Kind, val amount: BigDecimal, val at: Instant = Instant.now()): MongoConvert<MongoTransaction> {
    enum class Kind {
        INCOMING,
        OUTGOING
    }

    override val mongo: MongoTransaction
        get() = MongoTransaction(UUID.randomUUID(), player, kind, at, amount.toDouble())
}

fun transact(amount: BigDecimal, player: UUID, kind: Transaction.Kind): BigDecimal {
    Macrocosm.transactionHistory.remember(Transaction(player, kind, amount))
    return amount
}
