package space.maxus.macrocosm.item

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier

open class AbilityItem(
    override val type: ItemType, private val itemName: String, override var rarity: Rarity, override val base: Material,
    override var stats: Statistics,
    override val abilities: MutableList<ItemAbility> = mutableListOf(),
    override var specialStats: SpecialStatistics = SpecialStatistics(),
    private val metaModifier: (ItemMeta) -> ItemMeta = { it }
) : MacrocosmItem {
    override var amount: Int = 1
    override var stars: Int = 0
        set(value) {
            if(value > maxStars)
                return
            else field = value
        }
    override val id: Identifier = Identifier.macro(itemName.lowercase().replace(" ", "_"))
    override val name: Component = comp(itemName)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override var enchantments: HashMap<Enchantment, Int> = hashMapOf()

    override fun addExtraMeta(meta: ItemMeta) {
        metaModifier(meta)
    }

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = AbilityItem(type, itemName, rarity, base, stats.clone(), abilities, specialStats.clone(), metaModifier)
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        item.stars = stars
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
