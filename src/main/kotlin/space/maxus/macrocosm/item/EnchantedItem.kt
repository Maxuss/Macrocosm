package space.maxus.macrocosm.item

import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

class EnchantedItem(override val base: Material, override var rarity: Rarity, private var baseName: String = "Enchanted ${base.name.replace("_", " ").capitalized()}", thisId: String? = null, actualId: String? = null) : MacrocosmItem {
    override var stats: Statistics = Statistics.zero()
    override var specialStats: SpecialStatistics = SpecialStatistics()
    override val id: String = thisId ?: "ENCHANTED_${base.name}"
    override val type: ItemType = ItemType.OTHER
    override val name: Component = comp(baseName)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<ItemAbility> = mutableListOf()
    override val enchantments: HashMap<Enchantment, Int> = hashMapOf()

    private var actualBase: String = actualId ?: base.name

    override fun buildLore(lore: MutableList<Component>) {
        lore.add(comp("<yellow>Right click to view recipes").noitalic())
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putString("BaseItem", actualBase)
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as EnchantedItem
        base.actualBase = nbt.getString("BaseItem")
        return base
    }

    override fun addExtraMeta(meta: ItemMeta) {
        meta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
    }

    override fun clone(): MacrocosmItem {
        val e = EnchantedItem(base, rarity, baseName, id)
        e.actualBase = actualBase
        return e
    }
}
