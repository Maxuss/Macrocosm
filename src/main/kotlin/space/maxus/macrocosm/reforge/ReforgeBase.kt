package space.maxus.macrocosm.reforge

import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistics
import kotlin.math.max

abstract class ReforgeBase(
    override val name: String,
    override val abilityName: String? = null,
    override val abilityDescription: String? = null,
    override val applicable: List<ItemType>,
    private val baseStats: Statistics,
    private val multiplier: Float = 1f
) : Reforge {
    protected fun ensureRequirements(player: MacrocosmPlayer, vararg slots: EquipmentSlot): Boolean {
        val paper = player.paper ?: return false
        return slots.map {
            val mc = paper.inventory.getItem(it).macrocosm ?: return@map false
            mc.reforge == this
        }.any { it }
    }

    override fun stats(rarity: Rarity): Statistics {
        val clone = baseStats.clone()
        clone.multiply(1 + (multiplier * max(rarity.ordinal, 1)))
        return clone
    }
}
