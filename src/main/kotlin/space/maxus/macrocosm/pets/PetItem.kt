package space.maxus.macrocosm.pets

import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.item.AbilityItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

class PetItem(
    type: ItemType, itemName: String, rarity: Rarity, base: Material,
    description: String? = null,
    metaModifier: (ItemMeta) -> Unit = { },
): AbilityItem(
    type,
    itemName,
    rarity,
    base,
    Statistics.zero(),
    listOf(),
    SpecialStatistics(),
    0,
    listOf(),
    description,
    metaModifier = metaModifier) {

    override fun clone(): MacrocosmItem {
        return PetItem(type, itemName, rarity, base, description, metaModifier)
    }
}
