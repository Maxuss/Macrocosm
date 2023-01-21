package space.maxus.macrocosm.bazaar

import io.prometheus.client.Gauge
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.bazaar.ops.InstantBuyResult
import space.maxus.macrocosm.bazaar.ops.InstantSellResult
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.exceptions.MacrocosmThrowable
import space.maxus.macrocosm.metrics.MacrocosmMetrics
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.banking.Transaction
import space.maxus.macrocosm.players.banking.transact
import space.maxus.macrocosm.players.chat.ChatChannel
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.*
import space.maxus.macrocosm.util.general.Result
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Singleton object for managing bazaar operations
 */
object Bazaar {
    private val bazaarOpPool = Threading.newFixedPool(8)
    private val bazaarCoinsTotal by lazy { MacrocosmMetrics.gauge("bazaar_coins", "Total coins in Bazaar") }
    private val bazaarItemsTotal by lazy { MacrocosmMetrics.gauge("bazaar_items", "Total Items accumulated in Bazaar") }

    private fun metricsBuyAccumulatedQty(item: Identifier): Gauge {
        return MacrocosmMetrics.gauge("bazaar_buy_qty_${item.path}", "Bazaar Buy total $item quantity")
    }

    private fun metricsSellAccumulatedQty(item: Identifier): Gauge {
        return MacrocosmMetrics.gauge("bazaar_sell_qty_${item.path}", "Bazaar Sell total $item quantity")
    }

    /**
     * The containing table for bazaar operations
     */
    lateinit var table: BazaarTable; private set

    /**
     * Initializes the bazaar
     *
     * This init function is **Thread Safe** and can be used in [Threading.runEachConcurrently]
     */
    fun init() {
        table = BazaarTable.read()
        table.store()
    }

    /**
     * Gets all orders for the provided [player]
     */
    fun getOrdersForPlayer(player: UUID): List<BazaarOrder> {
        return table.itemData.values.map { entry ->
            entry.buy.filter { order -> order.createdBy == player }
                .withAll(entry.sell.filter { order -> order.createdBy == player })
        }.unwrapInner()
    }

    private fun concatPlayers(prefix: String, players: List<UUID>): String {
        return "<gray>${prefix}s: <br>${
            players.joinToString(separator = "<br>") { seller ->
                if (Macrocosm.playersLazy.contains(seller))
                    MacrocosmPlayer.loadPlayer(seller)?.rank?.playerName(Bukkit.getOfflinePlayer(seller).name ?: "NULL")
                        ?.str() ?: "<dark_gray>Unknown $prefix ($seller)" else "<dark_gray>Unknown $prefix ($seller)"
            }
        }"
    }

    /**
     * Instantly sells provided items
     *
     * @param player player that has started the instant sell operation
     * @param paper bukkit player mirror of the [MacrocosmPlayer]
     * @param item item that is being sold
     * @param qty quantity of items to sell
     */
    fun instantSell(player: MacrocosmPlayer, paper: Player, item: Identifier, qty: Int) {
        bazaarOpPool.execute {
            runCatchingReporting(paper) {
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Executing instant sell...")
                val result = tryDoInstantSell(player, item, qty, false).get()
                if (result.amountSold <= 0) {
                    player.sendMessage(ChatChannel.BAZAAR, "<red>Could not find offers to sell items to!")
                    return@execute
                } else if (result.amountSold < qty) {
                    player.sendMessage(
                        ChatChannel.BAZAAR,
                        "<red>Could not sell all items, only sold ${result.amountSold} out of $qty possible!"
                    )
                }

                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                player.purse += transact(
                    result.totalProfit * BazaarIntrinsics.INCOMING_TAX_MODIFIER,
                    player.ref,
                    Transaction.Kind.INCOMING
                )

                task(sync = true, delay = 0L) {
                    // drifting to sync thread
                    if (DemandQtyItemsQuery(item, result.amountSold).process(player, paper) !is Result) {
                        bazaarOpPool.execute {
                            player.sendMessage(
                                ChatChannel.BAZAAR,
                                "<yellow>Sold <green>${
                                    Formatting.withCommas(
                                        result.amountSold.toBigDecimal(),
                                        true
                                    )
                                }x<yellow> of ${BazaarElement.idToElement(item)?.name?.str()} for <gold>${
                                    Formatting.withCommas(
                                        result.totalProfit
                                    )
                                } Coins<yellow>! <green><bold><hover:show_text:'<gray>Orders affected: <green>${result.ordersAffected}<br>${
                                    concatPlayers(
                                        "Buyer",
                                        result.buyers
                                    )
                                }'>[HOVER FOR INFO]</hover>"
                            )
                            sound(Sound.ENTITY_PLAYER_LEVELUP) {
                                pitch = 2f
                                playFor(paper)
                            }
                            tryDoInstantSell(player, item, qty).get()
                        }
                    }
                }
                NULL
            }
        }
    }

    /**
     * Instantly buys provided items
     *
     * @param player player that has started the instant buy operation
     * @param paper bukkit player mirror of the [MacrocosmPlayer]
     * @param item item that is being bought
     * @param qty quantity of items to buy
     */
    fun instantBuy(player: MacrocosmPlayer, paper: Player, item: Identifier, qty: Int) {
        bazaarOpPool.execute {
            runCatchingReporting(paper) {
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Executing instant buy...")
                val preResult = tryDoInstantBuy(player, item, qty, false).get()
                if (preResult.amountBought <= 0) {
                    player.sendMessage(ChatChannel.BAZAAR, "<red>Could not find offers to buy from!")
                    return@execute
                } else if (preResult.amountBought < qty)
                    player.sendMessage(
                        ChatChannel.BAZAAR,
                        "<red>Could not find enough offers to buy from, only bought ${preResult.amountBought} out of $qty required!"
                    )
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")

                val transacted = preResult.coinsSpent
                if (!BazaarIntrinsics.ensurePlayerHasEnoughCoins(player, transacted)) {
                    player.sendMessage(ChatChannel.BAZAAR, "<red>Failed to process transaction, not enough coins!")
                    return@execute
                }

                tryDoInstantBuy(player, item, qty).get()

                player.purse -= transact(
                    transacted * BazaarIntrinsics.OUTGOING_TAX_MODIFIER,
                    player.ref,
                    Transaction.Kind.OUTGOING
                )

                sound(Sound.ENTITY_PLAYER_LEVELUP) {
                    pitch = 2f
                    playFor(paper)
                }
                task(sync = true, delay = 0L) {
                    // drifting to sync thread
                    val itemResult = BazaarElement.idToElement(item)!!
                    var amount = preResult.amountBought
                    val built = itemResult.build(player)
                    while (amount > 64) {
                        amount -= 64
                        val clone = built!!.clone()
                        clone.amount = 64
                        paper.giveOrDrop(clone)
                    }

                    val clone = built!!.clone()
                    clone.amount = amount
                    paper.giveOrDrop(clone)

                    player.sendMessage(
                        ChatChannel.BAZAAR,
                        "<yellow>Bought <green>${
                            Formatting.withCommas(
                                preResult.amountBought.toBigDecimal(),
                                true
                            )
                        }x<yellow> of ${BazaarElement.idToElement(item)?.name?.str()} for <gold>${
                            Formatting.withCommas(
                                preResult.coinsSpent
                            )
                        } Coins<yellow>! <green><bold><hover:show_text:'<gray>Orders affected: <green>${preResult.ordersAffected}<br>${
                            concatPlayers(
                                "Seller",
                                preResult.sellers
                            )
                        }'>[HOVER FOR INFO]</hover>"
                    )
                }
            }
        }
    }

    /**
     * Creates a buy order as the provided player
     *
     * @param player player that has started creating the buy order
     * @param paper bukkit player mirror of the [MacrocosmPlayer]
     * @param item item that is being bought
     * @param amount quantity of items to buy
     * @param pricePer price per single item
     */
    fun createBuyOrder(player: MacrocosmPlayer, paper: Player, item: Identifier, amount: Int, pricePer: Double) {
        bazaarOpPool.execute {
            runCatchingReporting(paper) {
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                val transacted = amount.toBigDecimal() * pricePer.toBigDecimal()
                if (!BazaarIntrinsics.ensurePlayerHasEnoughCoins(player, transacted)) {
                    player.sendMessage(ChatChannel.BAZAAR, "<red>Failed to process transaction, not enough coins!")
                    return@execute
                }

                player.purse -= transact(
                    transacted * BazaarIntrinsics.OUTGOING_TAX_MODIFIER,
                    player.ref,
                    Transaction.Kind.OUTGOING
                )
                bazaarCoinsTotal.inc(transacted.toDouble())

                player.sendMessage(ChatChannel.BAZAAR, "<gray>Setting up Buy Order...")
                try {
                    table.createOrder(BazaarBuyOrder(item, amount, pricePer, 0, mutableListOf(), player.ref, amount))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val name = BazaarElement.idToElement(item)!!
                player.sendMessage(
                    ChatChannel.BAZAAR,
                    "<yellow>Buy Order for <green>${
                        Formatting.withCommas(
                            amount.toBigDecimal(),
                            true
                        )
                    }<yellow> of ${name.name.str()}<yellow> for <gold>${Formatting.withCommas(pricePer.toBigDecimal())} coins<yellow> each setup!"
                )

                sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                    pitch = 1.7f
                    volume = 2f
                    playFor(paper)
                }

                taskRunLater(5L, sync = false) {
                    sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                        pitch = 1.4f
                        volume = 2f
                        playFor(paper)
                    }
                }

                metricsBuyAccumulatedQty(item).inc(amount.toDouble())
            }
        }
    }

    /**
     * Creates a sell order as the provided player
     *
     * @param player player that has started creating the sell order
     * @param paper bukkit player mirror of the [MacrocosmPlayer]
     * @param item item that is being sold
     * @param amount quantity of items to sell
     * @param pricePer price per single item
     */
    fun createSellOrder(player: MacrocosmPlayer, paper: Player, item: Identifier, amount: Int, pricePer: Double) {
        bazaarOpPool.execute {
            runCatchingReporting(paper) {
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                task(sync = true, delay = 0L) {
                    // drifting to synchronous environment
                    if (DemandQtyItemsQuery(item, amount).process(player, paper) !is Result) {
                        player.sendMessage(ChatChannel.BAZAAR, "<gray>Setting up Sell Order...")
                        table.createOrder(
                            BazaarSellOrder(
                                item,
                                amount,
                                pricePer,
                                0,
                                mutableListOf(),
                                player.ref,
                                amount
                            )
                        )
                        val name = BazaarElement.idToElement(item)!!
                        player.sendMessage(
                            ChatChannel.BAZAAR,
                            "<yellow>Sell Order for <green>${
                                Formatting.withCommas(
                                    amount.toBigDecimal(),
                                    true
                                )
                            }<yellow> of ${name.name.str()}<yellow> for <gold>${Formatting.withCommas(pricePer.toBigDecimal())} coins<yellow> each setup!"
                        )
                        bazaarItemsTotal.inc(amount.toDouble())
                        metricsSellAccumulatedQty(item).inc(amount.toDouble())
                    }
                }

                sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                    pitch = 1.7f
                    volume = 2f
                    playFor(paper)
                }

                taskRunLater(5L, sync = false) {
                    sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                        pitch = 1.4f
                        volume = 2f
                        playFor(paper)
                    }
                }
            }
        }
    }

    /**
     * Attempts to instantly buy provided items
     *
     * NOTE: this is a partially internal method, if you are looking for a way to do instant buy, look at [Bazaar.instantBuy] instead
     *
     * @param player player that initiated the instant buy
     * @param element item that is being bought
     * @param amount amount of items that are being bought
     * @param mutate whether to mutate (modify count of items sold etc.) existing orders
     *
     * @return completable future that returns the result if successful
     */
    fun tryDoInstantBuy(
        player: MacrocosmPlayer,
        element: Identifier,
        amount: Int,
        mutate: Boolean = true
    ): CompletableFuture<InstantBuyResult> {
        var satisfiedAmount = 0
        var currentPrice = BigDecimal(0)
        var affectedOffers = 0
        val sellers = mutableListOf<UUID>()
        return CompletableFuture.supplyAsync {
            table.iterateThroughOrdersSell(element) { order ->
                if (order.qty == 0)
                    return@iterateThroughOrdersSell false
                if (satisfiedAmount >= amount) {
                    return@iterateThroughOrdersSell true
                }
                if (player.purse < currentPrice) {
                    return@iterateThroughOrdersSell true
                }

                affectedOffers++

                // checking if order quantity exceeds needed
                if (order.qty + satisfiedAmount > amount) {
                    if (mutate) {
                        bazaarItemsTotal.dec((amount - satisfiedAmount).toDouble())
                        metricsSellAccumulatedQty(order.item).dec((amount - satisfiedAmount).toDouble())
                        order.sold += (amount - satisfiedAmount)
                        order.qty -= (amount - satisfiedAmount)
                        if (!order.buyers.contains(player.ref))
                            order.buyers.add(player.ref)
                    }
                    if (!sellers.contains(order.createdBy))
                        sellers.add(order.createdBy)
                    // adding coins
                    val toAdd = (amount - satisfiedAmount).toBigDecimal() * order.pricePer.toBigDecimal()
                    if (player.purse < currentPrice + toAdd) {
                        return@iterateThroughOrdersSell true
                    }
                    // adding amount
                    satisfiedAmount = amount
                    currentPrice += toAdd
                    if (mutate)
                        bazaarCoinsTotal.inc(toAdd.toDouble())
                    return@iterateThroughOrdersSell true
                }
                // otherwise just adding possible amount and price
                satisfiedAmount += order.qty
                currentPrice += order.totalPrice
                if (mutate) {
                    bazaarItemsTotal.dec(satisfiedAmount.toDouble())
                    metricsSellAccumulatedQty(order.item).dec(satisfiedAmount.toDouble())
                    bazaarCoinsTotal.inc(order.totalPrice.toDouble())
                    order.sold += order.qty
                    order.qty = 0
                    order.buyers.add(player.ref)
                }
                if (!sellers.contains(order.createdBy))
                    sellers.add(order.createdBy)
                if (player.purse < currentPrice) {
                    return@iterateThroughOrdersSell true
                }
                return@iterateThroughOrdersSell false
            }

            return@supplyAsync InstantBuyResult(satisfiedAmount, currentPrice, affectedOffers, sellers)
        }
    }

    /**
     * Attempts to instantly sell provided items
     *
     * NOTE: this is a partially internal method, if you are looking for a way to do instant sell, look at [Bazaar.instantSell] instead
     *
     * @param player player that initiated the instant sell
     * @param element item that is being sold
     * @param amount amount of items that are being sold
     * @param mutate whether to mutate (modify count of items sold etc.) existing orders
     *
     * @return completable future that returns the result if successful
     */
    fun tryDoInstantSell(
        player: MacrocosmPlayer,
        element: Identifier,
        amount: Int,
        mutate: Boolean = true
    ): CompletableFuture<InstantSellResult> {
        var leftToSell = amount
        var currentProfit = BigDecimal(0)
        var affectedOffers = 0
        val buyers = mutableListOf<UUID>()
        return CompletableFuture.supplyAsync {
            table.iterateThroughOrdersBuy(element) { order ->
                if (order.qty == 0)
                    return@iterateThroughOrdersBuy false
                if (leftToSell <= 0) {
                    return@iterateThroughOrdersBuy true
                }
                affectedOffers++
                // checking if there are more than enough items to sell to order
                if (order.qty > leftToSell) {
                    currentProfit += order.pricePer.toBigDecimal() * leftToSell.toBigDecimal()
                    if (mutate) {
                        bazaarItemsTotal.dec(leftToSell.toDouble())
                        metricsBuyAccumulatedQty(order.item).dec(leftToSell.toDouble())
                        bazaarCoinsTotal.dec(order.pricePer * leftToSell)
                        order.qty -= leftToSell
                        order.bought += leftToSell
                        if (order.sellers.contains(player.ref))
                            order.sellers.add(player.ref)
                    }
                    leftToSell = 0
                    if (!buyers.contains(order.createdBy))
                        buyers.add(order.createdBy)
                    return@iterateThroughOrdersBuy true
                }
                currentProfit += order.totalPrice
                val diff = order.qty
                leftToSell -= order.qty
                if (mutate) {
                    bazaarItemsTotal.dec(leftToSell.toDouble())
                    metricsBuyAccumulatedQty(order.item).dec(leftToSell.toDouble())
                    bazaarCoinsTotal.dec(order.totalPrice.toDouble())
                    order.qty = 0
                    order.bought += diff
                }
                if (!buyers.contains(order.createdBy))
                    buyers.add(order.createdBy)
                return@iterateThroughOrdersBuy false
            }

            return@supplyAsync InstantSellResult(amount - leftToSell, currentProfit, affectedOffers, buyers)
        }
    }

    /**
     * An error that occurs when processing bazaar operation
     */
    class BazaarError(currentQuery: Query, parent: Throwable?, orMessage: String? = null) : MacrocosmThrowable(
        "BAZAAR_ERROR",
        """Unhandled error in bazaar operation "${currentQuery.id}": ${parent?.message ?: orMessage}"""
    )

    /**
     * An interface for bazaar queries
     */
    interface Query {
        /**
         * ID of this query
         */
        val id: String

        /**
         * Processes this query returning dynamic value based on the implementation
         */
        fun process(player: MacrocosmPlayer, paper: Player): Any
    }

    /**
     * Demands [coins] amount of coins from the player.
     *
     * Possible returns by [process]:
     *  * Int (1) => the operation went successfully
     *  * Result (failure) => player does not have enough coins
     */
    class DemandCoinsQuery(val coins: BigDecimal) : Query {
        override val id = "QUERY_DEMAND_COINS"
        override fun process(player: MacrocosmPlayer, paper: Player): Any {
            if (player.purse >= coins) {
                player.purse -= transact(coins, player.ref, Transaction.Kind.OUTGOING)
                return 1
            }
            return Result.failure("Not enough coins, expected ${Formatting.withCommas(coins)} coins.")
        }
    }

    /**
     * Demands all items of type [element] from player
     *
     * Possible returns by [process]:
     *  * Int(amount) => amount of items were removed
     *  * Result(failure) => player does not have any items of the type [element]
     *  * throws BazaarError => invalid operation provoked by you (e.g. invalid [element] ID)
     */
    class DemandAllItemsQuery(val element: Identifier) : Query {
        override val id = "QUERY_DEMAND_ITEMS_ALL"

        override fun process(player: MacrocosmPlayer, paper: Player): Any {
            val item = BazaarElement.idToElement(element) ?: throw BazaarError(
                this,
                null,
                "Could not find item $element in any registry"
            )
            val built = item.build(player) ?: throw BazaarError(
                this,
                null,
                "Failed to build macrocosm item, returned null (item: $element)!"
            )
            if (!paper.inventory.containsAtLeast(built, 1)) {
                return Result.failure("Not enough items, expected at least 1 item.")
            }
            val amount = paper.inventory.sumOf { if (it.isSimilar(built)) it.amount else 0 }
            var removed = paper.inventory.removeItemAnySlot(built)
            while (removed.isEmpty()) {
                removed = paper.inventory.removeItemAnySlot(built)
            }
            return amount
        }
    }

    /**
     * Demands a certain quantity of items of type [element] from player
     *
     * Possible returns by [process]:
     *  * Result(failure) => player does not have enough items
     *  * Int(1) => operation successful
     *  * throws BazaarError => invalid operation provoked by you (e.g. invalid [element] ID)
     */
    class DemandQtyItemsQuery(val element: Identifier, val amount: Int) : Query {
        override val id = "QUERY_DEMAND_ITEMS_QUANTITY"

        override fun process(player: MacrocosmPlayer, paper: Player): Any {
            val item = BazaarElement.idToElement(element) ?: throw BazaarError(
                this,
                null,
                "Could not find item $element in any registry"
            )
            val built = item.build(player) ?: throw BazaarError(
                this,
                null,
                "Failed to build macrocosm item, returned null (item: $element)!"
            )
            if (!paper.inventory.containsAtLeast(built, amount)) {
                player.sendMessage(ChatChannel.BAZAAR, "<red>Not enough items, expected $amount!")
                return Result.failure("Not enough items, expected $amount.")
            }
            var count = amount
            while (count > 64) {
                built.amount = 64
                paper.inventory.removeItemAnySlot(built)
                count -= 64
            }
            built.amount = count
            paper.inventory.removeItemAnySlot(built)
            return 1
        }
    }
}
