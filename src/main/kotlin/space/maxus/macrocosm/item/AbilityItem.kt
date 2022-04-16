package space.maxus.macrocosm.item

import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeRegistry
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

class AbilityItem(
    override val type: ItemType, itemName: String, override var rarity: Rarity, override val base: Material,
    override var stats: Statistics,
    override val abilities: MutableList<ItemAbility> = mutableListOf()
) : MacrocosmItem {
    override val name: Component = comp(itemName)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        rarityUpgraded = nbt.getBoolean("RarityUpgraded")
        if (rarityUpgraded)
            rarity = rarity.next()

        val reforge = nbt.getString("Reforge")
        if (reforge != "NULL") {
            reforge(ReforgeRegistry.find(reforge)!!)
        }
        return this
    }
}
