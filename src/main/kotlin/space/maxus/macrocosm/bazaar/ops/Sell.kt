package space.maxus.macrocosm.bazaar.ops

import java.math.BigDecimal
import java.util.*

/**
 * Result for performing instant sell
 */
data class InstantSellResult(
    /**
     * Amount of items sold
     */
    val amountSold: Int,
    /**
     * Amount of coins earned by the player
     */
    val totalProfit: BigDecimal,
    /**
     * Amount of buy orders affected
     */
    val ordersAffected: Int,
    /**
     * All the buyers
     */
    val buyers: List<UUID>
)
