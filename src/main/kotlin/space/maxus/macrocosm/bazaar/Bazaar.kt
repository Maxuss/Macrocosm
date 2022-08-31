package space.maxus.macrocosm.bazaar

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
import space.maxus.macrocosm.database
import space.maxus.macrocosm.exceptions.MacrocosmThrowable
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.banking.Transaction
import space.maxus.macrocosm.players.banking.transact
import space.maxus.macrocosm.players.chat.ChatChannel
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.general.Result
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.runCatchingReporting
import space.maxus.macrocosm.util.unwrapInner
import space.maxus.macrocosm.util.withAll
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture

object Bazaar {
    val bazaarOpPool = Threading.newFixedPool(8)

    lateinit var table: BazaarTable; private set

    fun init() {
        Threading.runAsyncRaw {
            table = BazaarTable.readSelf(database)
        }
    }

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

    fun instantSell(player: MacrocosmPlayer, paper: Player, item: Identifier, qty: Int) {
        bazaarOpPool.execute {
            runCatchingReporting(paper) {
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Executing instant sell...")
                val result = tryDoInstantSell(item, qty, false).get()
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

//                    val itemBuilt = BazaarElement.idToElement(item)!!.build(player)!!
//                    var amount = result.amountSold
//                    while(amount > 64) {
//                        val clone = itemBuilt.clone()
//                        clone.amount = 64
//                        amount -= 64
//                        paper.inventory.removeItemAnySlot(clone)
//                    }
//                    val clone = itemBuilt.clone()
//                    clone.amount = amount
//                    paper.inventory.removeItemAnySlot(clone)

                task(sync = true, delay = 0L) {
                    // drifting to sync thread
                    if (DemandQtyItemsQuery(item, result.amountSold).demand(player, paper) !is Result) {
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
                            tryDoInstantSell(item, qty).get()
                        }
                    }
                }
            }
        }
    }

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
            }
        }
    }

    fun createSellOrder(player: MacrocosmPlayer, paper: Player, item: Identifier, amount: Int, pricePer: Double) {
        bazaarOpPool.execute {
            runCatchingReporting(paper) {
                player.sendMessage(ChatChannel.BAZAAR, "<gray>Processing transaction...")
                task(sync = true, delay = 0L) {
                    // drifting to synchronous environment
                    if (DemandQtyItemsQuery(item, amount).demand(player, paper) !is Result) {
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
                        order.sold += (amount - satisfiedAmount)
                        order.qty -= (amount - satisfiedAmount)
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
                    return@iterateThroughOrdersSell true
                }
                // otherwise just adding possible amount and price
                satisfiedAmount += order.qty
                currentPrice += order.totalPrice
                if (mutate) {
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

    fun tryDoInstantSell(
        element: Identifier,
        amount: Int,
        mutate: Boolean = true
    ): CompletableFuture<InstantSellResult> {
        var leftToSell = amount
        var currentProfit = BigDecimal(0)
        var affectedOffers = 0
        val sellers = mutableListOf<UUID>()
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
                        order.qty -= leftToSell
                        order.bought += leftToSell
                    }
                    leftToSell = 0
                    if (!sellers.contains(order.createdBy))
                        sellers.add(order.createdBy)
                    return@iterateThroughOrdersBuy true
                }
                currentProfit += order.totalPrice
                val diff = order.qty
                leftToSell -= order.qty
                if (mutate) {
                    order.qty = 0
                    order.bought += diff
                }
                if (!sellers.contains(order.createdBy))
                    sellers.add(order.createdBy)
                return@iterateThroughOrdersBuy false
            }

            return@supplyAsync InstantSellResult(amount - leftToSell, currentProfit, affectedOffers, sellers)
        }
    }


    class BazaarError(currentQuery: Query, parent: Throwable?, orMessage: String? = null) : MacrocosmThrowable(
        "BAZAAR_ERROR",
        """Unhandled error in bazaar operation "${currentQuery.id}": ${parent?.message ?: orMessage}"""
    )

    interface Query {
        val id: String
        fun demand(player: MacrocosmPlayer, paper: Player): Any
    }

    class DemandCoinsQuery(val coins: BigDecimal) : Query {
        override val id = "QUERY_DEMAND_COINS"
        override fun demand(player: MacrocosmPlayer, paper: Player): Any {
            if (player.purse >= coins) {
                player.purse -= transact(coins, player.ref, Transaction.Kind.OUTGOING)
                return 1
            }
            return Result.failure("Not enough coins, expected ${Formatting.withCommas(coins)} coins.")
        }
    }

    class DemandAllItemsQuery(val element: Identifier) : Query {
        override val id = "QUERY_DEMAND_ITEMS_ALL"

        override fun demand(player: MacrocosmPlayer, paper: Player): Any {
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

    class DemandQtyItemsQuery(val element: Identifier, val amount: Int) : Query {
        override val id = "QUERY_DEMAND_ITEMS_QUANTITY"

        override fun demand(player: MacrocosmPlayer, paper: Player): Any {
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
