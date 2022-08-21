package space.maxus.macrocosm.players.banking

import space.maxus.macrocosm.Macrocosm
import java.math.BigDecimal
import java.util.*

class Transaction(val player: UUID, val kind: Kind, val amount: BigDecimal) {
    enum class Kind {
        INCOMING,
        OUTGOING
    }
}

fun transact(amount: BigDecimal, player: UUID, kind: Transaction.Kind): BigDecimal {
    Macrocosm.transactionHistory.remember(Transaction(player, kind, amount))
    return amount
}
