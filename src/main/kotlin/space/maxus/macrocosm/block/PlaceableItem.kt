package space.maxus.macrocosm.block

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.generators.HybridBlockModelGenerator
import space.maxus.macrocosm.item.AbstractMacrocosmItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Identifier

open class PlaceableItem(
    id: Identifier,
    private val blockId: Identifier,
    override var name: Component,
    override var rarity: Rarity
) : AbstractMacrocosmItem(id, ItemType.OTHER) {
    override val base: Material = Material.PAPER

    override fun clone(): MacrocosmItem {
        return PlaceableItem(id, blockId, name, rarity)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        meta.setCustomModelData(HybridBlockModelGenerator.model(blockId)?.customModelData ?: return)
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        return false
    }

}
