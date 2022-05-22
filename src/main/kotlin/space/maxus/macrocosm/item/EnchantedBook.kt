package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.id

private fun rarityFromEnchants(ench: HashMap<Enchantment, Int>): Rarity {
    val (_, lvl) = ench.maxByOrNull { it.value } ?: return Rarity.SPECIAL
    return if (lvl >= 8) Rarity.GODLIKE
    else if (lvl >= 7) Rarity.MYTHIC
    else if (lvl >= 6) Rarity.LEGENDARY
    else if (lvl >= 5) Rarity.EPIC
    else if (lvl >= 4) Rarity.RARE
    else if (lvl >= 3) Rarity.UNCOMMON
    else Rarity.COMMON
}

class EnchantedBook(override val enchantments: HashMap<Enchantment, Int> = hashMapOf()) : MacrocosmItem {
    override var stats: Statistics get() = Statistics.zero(); set(_) {}
    override var specialStats: SpecialStatistics get() = SpecialStatistics(); set(_) {}
    override var amount: Int = 1
    override var stars: Int = 0
    override val id: Identifier = id("enchanted_book")
    override val type: ItemType = ItemType.OTHER
    override var name: Component = comp(enchantments.maxByOrNull { it.value }?.key?.name ?: "Enchanted Book")
    override val base: Material = Material.ENCHANTED_BOOK
    override var rarity: Rarity = rarityFromEnchants(enchantments)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<ItemAbility> = mutableListOf()
    override val runes: HashMap<ApplicableRune, RuneState> = HashMap()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0

    override fun addPotatoBooks(amount: Int) {

    }

    override fun stats(): Statistics {
        return Statistics.zero()
    }

    override fun buildLore(lore: MutableList<Component>) {
        lore.add(0, "".toComponent())
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        if (!enchantment.levels.contains(level))
            return false
        enchantUnsafe(enchantment, level)
        return true
    }

    override fun reforge(ref: Reforge) {

    }

    override fun transfer(to: MacrocosmItem) {
        for ((enchant, level) in enchantments) {
            to.enchant(enchant, level)
        }
    }

    override fun clone(): MacrocosmItem {
        val clone = EnchantedBook(enchantments)
        clone.rarity = rarityFromEnchants(clone.enchantments)
        clone.name = comp(enchantments.maxByOrNull { it.value }?.key?.name ?: "Enchanted Book")
        return clone
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt)
        base.name = comp(enchantments.maxByOrNull { it.value }?.key?.name ?: "Enchanted Book")
        return base
    }
}
