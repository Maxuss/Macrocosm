package space.maxus.macrocosm.accessory

import org.bukkit.Material
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.str

class TexturedAccessoryItem(id: String, name: String, rarity: Rarity, stats: Statistics, abilities: List<RegistryPointer>): AccessoryItem(id, name, rarity, stats, abilities.toMutableList(), null, Material.PAPER) {
    override fun clone(): MacrocosmItem {
        return TexturedAccessoryItem(
            id.path,
            name.str(),
            rarity,
            stats,
            abilities
        )
    }
}
