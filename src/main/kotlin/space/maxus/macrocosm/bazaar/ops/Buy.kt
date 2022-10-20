package space.maxus.macrocosm.bazaar.ops

import java.math.BigDecimal
import java.util.*

/**
 * Result for performing instant buy
 */
data class InstantBuyResult(
    /**
     * Amount of items bought
     */
    val amountBought: Int,
    /**
     * Amount of coins spent by player
     */
    val coinsSpent: BigDecimal,
    /**
     * Amount of sell orders affected
     */
    val ordersAffected: Int,
    /**
     * All the sellers affected
     */
    val sellers: List<UUID>
)
