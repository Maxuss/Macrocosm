package space.maxus.macrocosm.item

import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

class KillCountingItem(
    type: ItemType, itemName: String, rarity: Rarity, base: Material,
    stats: Statistics,
    abilities: MutableList<MacrocosmAbility> = mutableListOf(),
    specialStats: SpecialStatistics = SpecialStatistics(),
    breakingPower: Int = 0,
    runeTypes: List<RuneSlot> = listOf(),
    description: String? = null,
    metaModifier: (ItemMeta) -> Unit = { },
) : AbilityItem(
    type,
    itemName,
    rarity,
    base,
    stats,
    abilities,
    specialStats,
    breakingPower,
    runeTypes,
    description,
    metaModifier = metaModifier
), KillStorageItem {
    override var kills: Int = 0

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putInt("Kills", kills)
    }

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = KillCountingItem(
            type,
            itemName,
            rarity,
            base,
            stats.clone(),
            abilities,
            specialStats.clone(),
            metaModifier = metaModifier,
            description = description
        )
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        item.stars = stars
        item.breakingPower = breakingPower
        item.runes.putAll(runes)
        item.kills = kills
        return item
    }

    override fun transfer(to: MacrocosmItem) {
        super.transfer(to)
        if (to is KillCountingItem)
            to.kills = kills
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as KillCountingItem
        base.kills = nbt.getInt("Kills")
        return base
    }
}
