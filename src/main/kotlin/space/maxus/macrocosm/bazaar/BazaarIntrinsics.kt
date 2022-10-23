package space.maxus.macrocosm.bazaar

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import java.math.BigDecimal

/**
 * Internal data relevant to the bazaar
 */
@ApiStatus.Internal
object BazaarIntrinsics {
    /**
     * Modifier of coins the bazaar is taking when buying items or creating buy orders (1.125%)
     */
    @JvmStatic
    val OUTGOING_TAX_MODIFIER: BigDecimal = BigDecimal.valueOf(1.0125)

    /**
     * Modifiers of coins the bazaar is taking when selling items or creating sell orders (1.125%)
     */
    @JvmStatic
    val INCOMING_TAX_MODIFIER: BigDecimal = BigDecimal.valueOf(0.9875)

    /**
     * Ensures the player has enough items of type [item] in their inventory
     */
    @JvmStatic
    fun ensurePlayerHasEnoughItems(player: MacrocosmPlayer, paper: Player, item: Identifier, amount: Int): Boolean {
        val element = BazaarElement.idToElement(item)?.build(player) ?: return false
        return paper.inventory.containsAtLeast(element, amount)
    }

    /**
     * Ensures the player has enough coins
     */
    @JvmStatic
    fun ensurePlayerHasEnoughCoins(player: MacrocosmPlayer, amount: BigDecimal): Boolean {
        return player.purse > amount * OUTGOING_TAX_MODIFIER
    }
}
