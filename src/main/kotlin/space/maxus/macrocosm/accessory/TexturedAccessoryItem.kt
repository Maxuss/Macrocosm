package space.maxus.macrocosm.accessory

import org.bukkit.Material
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.str
import java.util.*

/**
 * An accessory item with a custom texture
 */
class TexturedAccessoryItem(id: String, name: String, rarity: Rarity, stats: Statistics, abilities: List<RegistryPointer>, uuid: UUID = UUID.randomUUID(), family: String = extractAccessoryFamily(id)): AccessoryItem(id, name, rarity, stats, abilities.toMutableList(), null, Material.PAPER, uuid, family) {
    override fun clone(): MacrocosmItem {
        return TexturedAccessoryItem(
            id.path,
            name.str(),
            rarity,
            stats,
            abilities,
            UUID.randomUUID(),
            family
        )
    }
}
