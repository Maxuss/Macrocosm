package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text

class StealingEnchantment(
    name: String,
    private val stat: Statistic,
    private val amount: Float,
    val regain: (MacrocosmPlayer, Pair<Float, Boolean>) -> Unit,
    private val actualDescription: String? = null,
    conflicts: List<String> = listOf(),
    applicable: List<ItemType> = ItemType.melee(),
    levels: IntRange = 1..7
) : EnchantmentBase(name, "", levels, applicable, conflicts = conflicts) {
    override fun description(level: Int): List<Component> {
        val str = actualDescription?.replace("{{amount}}", Formatting.stats((amount * level).toBigDecimal()))
            ?: "<gray>Regain <green>${Formatting.stats((amount * level).toBigDecimal())}%<gray> of your max ${stat.display}<gray> on hit."
        val reduced = str.reduceToList(25).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlank() }
        return reduced
    }

    @EventHandler
    fun onHit(e: PlayerDealDamageEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return
        val amount = amount * lvl
        regain(e.player, Pair(amount, e.crit))
    }
}
