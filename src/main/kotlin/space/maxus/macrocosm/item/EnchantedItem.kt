package space.maxus.macrocosm.item

import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

class EnchantedItem(override val base: Material, override var rarity: Rarity, private var baseName: String, baseId: String? = null) : MacrocosmItem {
    override var stats: Statistics = Statistics.zero()
    override var specialStats: SpecialStatistics = SpecialStatistics()
    override val id: String = baseId ?: "ENCHANTED_${base.name}"
    override val type: ItemType = ItemType.OTHER
    override val name: Component = comp(baseName)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<ItemAbility> = mutableListOf()
    override val enchantments: HashMap<Enchantment, Int> = hashMapOf()

    override fun buildLore(lore: MutableList<Component>) {
        lore.add(comp("<yellow>Right click to view recipes").noitalic())
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putString("EnchantedItem", baseName)
        cmp.putInt("EnchantedRarity", if(rarityUpgraded) rarity.previous().ordinal else rarity.ordinal)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        meta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
    }

    override fun reforge(ref: Reforge) {

    }

    override fun upgradeRarity(): Boolean {
        return false
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        return false
    }

    override fun clone(): MacrocosmItem {
        return EnchantedItem(base, rarity, baseName)
    }
}
