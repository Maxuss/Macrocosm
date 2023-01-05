package space.maxus.macrocosm.block

import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.item.AbstractMacrocosmItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Identifier

open class PlaceableItem(
    id: Identifier,
    val blockId: Identifier,
    override val base: Material,
    override var name: Component,
    override var rarity: Rarity
) : AbstractMacrocosmItem(id, ItemType.OTHER) {
    override fun clone(): MacrocosmItem {
        return PlaceableItem(id, blockId, base, name, rarity)
    }
}
