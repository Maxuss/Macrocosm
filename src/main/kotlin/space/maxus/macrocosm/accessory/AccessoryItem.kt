package space.maxus.macrocosm.accessory

import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.item.AbstractMacrocosmItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id
import java.io.Serializable

open class AccessoryItem(id: String, name: String, override var rarity: Rarity, override var stats: Statistics, override val abilities: MutableList<RegistryPointer>, private val headSkin: String? = null, override val base: Material = if(headSkin == null) error("Unassigned Material for accessory") else Material.PLAYER_HEAD): AbstractMacrocosmItem(id(id), ItemType.ACCESSORY), Serializable {
    override var name: Component = text(name)

    override fun clone(): MacrocosmItem {
        return AccessoryItem(
            this.id.path,
            name.str(),
            rarity,
            stats.clone(),
            abilities,
            headSkin,
            base
        )
    }
}
