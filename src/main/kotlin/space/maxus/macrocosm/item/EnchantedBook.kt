package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id

private fun rarityFromEnchants(ench: HashMap<Identifier, Int>): Rarity {
    if (ench.values.isEmpty())
        return Rarity.COMMON
    val lvl = ench.values.max()
    return if (lvl >= 8) Rarity.DIVINE
    else if (lvl >= 7) Rarity.MYTHIC
    else if (lvl >= 6) Rarity.LEGENDARY
    else if (lvl >= 5) Rarity.EPIC
    else if (lvl >= 4) Rarity.RARE
    else if (lvl >= 3) Rarity.UNCOMMON
    else Rarity.COMMON
}

class EnchantedBook(override var enchantments: HashMap<Identifier, Int> = hashMapOf()) :
    AbstractMacrocosmItem(id("enchanted_book"), ItemType.OTHER) {
    override var name: Component =
        text(enchantments.maxByOrNull { it.value }?.key?.let { Registry.ENCHANT.findOrNull(it)?.name }
            ?: "Enchanted Book")
    override val base: Material = Material.ENCHANTED_BOOK
    override var rarity: Rarity = rarityFromEnchants(enchantments)
    override fun addPotatoBooks(amount: Int) {

    }

    override fun stats(player: MacrocosmPlayer?): Statistics {
        return Statistics.zero()
    }

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
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
            to.enchant(Registry.ENCHANT.find(enchant), level)
        }
    }

    override fun clone(): MacrocosmItem {
        val clone = EnchantedBook()
        clone.enchantments.putAll(this.enchantments)
        clone.rarity = rarityFromEnchants(clone.enchantments)
        clone.name = text(enchantments.maxByOrNull { it.value }?.key?.let { Registry.ENCHANT.findOrNull(it)?.name }
            ?: "Enchanted Book")
        return clone
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt)
        base.name = text(enchantments.maxByOrNull { it.value }?.key?.let { Registry.ENCHANT.findOrNull(it)?.name }
            ?: "Enchanted Book")
        return base
    }
}
