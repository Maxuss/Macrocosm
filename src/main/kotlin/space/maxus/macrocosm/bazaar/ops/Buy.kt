package space.maxus.macrocosm.bazaar.ops

import java.math.BigDecimal
import java.util.*

data class InstantBuyResult(val amountBought: Int, val coinsSpent: BigDecimal, val ordersAffected: Int, val sellers: List<UUID>)
