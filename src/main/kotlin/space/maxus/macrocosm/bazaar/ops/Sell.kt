package space.maxus.macrocosm.bazaar.ops

import java.math.BigDecimal
import java.util.*

data class InstantSellResult(val amountSold: Int, val totalProfit: BigDecimal, val ordersAffected: Int, val buyers: List<UUID>)
