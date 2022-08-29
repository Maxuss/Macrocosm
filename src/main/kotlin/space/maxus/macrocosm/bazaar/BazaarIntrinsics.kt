package space.maxus.macrocosm.bazaar

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import java.math.BigDecimal

@ApiStatus.Internal
object BazaarIntrinsics {
    @JvmStatic
    val OUTGOING_TAX_MODIFIER: BigDecimal = BigDecimal.valueOf(1.0125)

    @JvmStatic
    val INCOMING_TAX_MODIFIER: BigDecimal = BigDecimal.valueOf(0.9875)

    @JvmStatic
    fun ensurePlayerHasEnoughItems(player: MacrocosmPlayer, paper: Player, item: Identifier, amount: Int): Boolean {
        val element = BazaarElement.idToElement(item)?.build(player) ?: return false
        return paper.inventory.containsAtLeast(element, amount)
    }

    @JvmStatic
    fun ensurePlayerHasEnoughCoins(player: MacrocosmPlayer, amount: BigDecimal): Boolean {
        return player.purse > amount * OUTGOING_TAX_MODIFIER
    }
}
