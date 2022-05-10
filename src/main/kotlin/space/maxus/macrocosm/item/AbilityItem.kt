package space.maxus.macrocosm.item

import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier

class AbilityItem(
    override val type: ItemType, private val itemName: String, override var rarity: Rarity, override val base: Material,
    override var stats: Statistics,
    override val abilities: MutableList<ItemAbility> = mutableListOf(),
    override var specialStats: SpecialStatistics = SpecialStatistics()
) : MacrocosmItem {
    override var amount: Int = 1
    override val id: Identifier = Identifier.macro(itemName.lowercase().replace(" ", "_"))
    override val name: Component = comp(itemName)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override var enchantments: HashMap<Enchantment, Int> = hashMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = AbilityItem(type, itemName, rarity, base, stats.clone(), abilities, specialStats.clone())
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        return item
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is MacrocosmItem && other.id == id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + stats.hashCode()
        result = 31 * result + abilities.hashCode()
        result = 31 * result + specialStats.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + rarityUpgraded.hashCode()
        result = 31 * result + (reforge?.hashCode() ?: 0)
        result = 31 * result + enchantments.hashCode()
        return result
    }
}
