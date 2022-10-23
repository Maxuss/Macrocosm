package space.maxus.macrocosm.bazaar.ops

/**
 * Type of bazaar operation
 */
enum class BazaarOp {
    /**
     * Creates a buy order
     */
    CREATE_BUY_ORDER,

    /**
     * Creates a sell order
     */
    CREATE_SELL_ORDER,

    /**
     * Performs an instant buy
     */
    DO_INSTANT_BUY,

    /**
     * Performs and instant sell
     */
    DO_INSTANT_SELL,
    ;
}
