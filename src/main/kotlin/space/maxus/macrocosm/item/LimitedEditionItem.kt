package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text

class LimitedEditionItem(
    ty: ItemType,
    val nameStr: String,
    rarity: Rarity,
    base: Material,
    stats: Statistics,
    abilities: MutableList<MacrocosmAbility> = mutableListOf(),
    specialStats: SpecialStatistics = SpecialStatistics(),
    breakingPower: Int = 0,
    runeTypes: List<RuneSlot> = listOf(),
    metaModifier: (ItemMeta) -> Unit = { }
): AbilityItem(ty, nameStr, rarity, base, stats, abilities, specialStats, breakingPower, runeTypes, metaModifier = metaModifier) {
    var edition: Int = -1
    var givenBy: Component = Component.text("null")
    var givenTo: Component = Component.text("null")

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = LimitedEditionItem(
            type,
            itemName,
            rarity,
            base,
            stats.clone(),
            abilities,
            specialStats.clone(),
            metaModifier = metaModifier
        )
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        item.stars = stars
        item.breakingPower = breakingPower
        item.runes.putAll(runes)
        item.givenBy = givenBy
        item.givenTo = givenTo
        item.edition = edition
        return item
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as LimitedEditionItem
        base.edition = nbt.getInt("Edition")
        base.givenBy = text(nbt.getString("GivenBy"))
        base.givenTo = text(nbt.getString("GivenTo"))
        return base
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        super.addExtraNbt(cmp)
        cmp.putInt("Edition", edition)
        cmp.putString("GivenBy", MiniMessage.miniMessage().serialize(givenBy))
        cmp.putString("GivenTo", MiniMessage.miniMessage().serialize(givenTo))
    }

    override fun buildLore(lore: MutableList<Component>) {
        super.buildLore(lore)
        lore.add(text("<gray>To: ").append(givenTo).noitalic())
        lore.add(text("<gray>From: ").append(givenBy).noitalic())
        lore.add("".toComponent())
        lore.add(text("<dark_gray>Edition #$edition").noitalic())
    }
}
