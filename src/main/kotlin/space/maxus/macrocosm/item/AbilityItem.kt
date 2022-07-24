package space.maxus.macrocosm.item

import com.google.common.collect.Multimap
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.annotations.PreviewFeature
import space.maxus.macrocosm.util.multimap

open class AbilityItem(
    override val type: ItemType,
    protected val itemName: String,
    override var rarity: Rarity,
    override val base: Material,
    override var stats: Statistics,
    override val abilities: MutableList<MacrocosmAbility> = mutableListOf(),
    override var specialStats: SpecialStatistics = SpecialStatistics(),
    override var breakingPower: Int = 0,
    runeTypes: List<RuneSlot> = listOf(),
    protected val description: String? = null,
    id: Identifier? = null,
    protected val metaModifier: (ItemMeta) -> Unit = { },
) : MacrocosmItem {
    override var amount: Int = 1
    override var stars: Int = 0
        set(value) {
            if (value > maxStars)
                return
            else field = value
        }
    override var name: Component = text(itemName)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override var enchantments: HashMap<Enchantment, Int> = hashMapOf()
    override val runes: Multimap<RuneSlot, RuneState> = multimap<RuneSlot, RuneState>().apply {
        for (ty in runeTypes) {
            put(ty, RuneState.EMPTY)
        }
    }
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var dye: Dye? = null
    override var skin: SkullSkin? = null

    @PreviewFeature
    override var isDungeonised: Boolean = false

    override var tempColor: Int? = null
    override var tempSkin: String? = null

    override fun buildLore(lore: MutableList<Component>) {
        super.buildLore(lore)
        if (description != null) {
            lore.addAll(description.reduceToList().map { text("<dark_gray>$it").noitalic() })
        }
    }

    override fun addExtraMeta(meta: ItemMeta) {
        metaModifier(meta)
    }

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = AbilityItem(
            type,
            itemName,
            rarity,
            base,
            stats.clone(),
            abilities,
            specialStats.clone(),
            metaModifier = metaModifier,
            description = description,
            id = id
        )
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        item.stars = stars
        item.breakingPower = breakingPower
        item.runes.putAll(runes)
        return item
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is MacrocosmItem && other.id == id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + stats.hashCode()
        result = 31 * result + abilities.hashCode()
        result = 31 * result + specialStats.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + rarityUpgraded.hashCode()
        result = 31 * result + (reforge?.hashCode() ?: 0)
        result = 31 * result + enchantments.hashCode()
        return result
    }

    override val id: Identifier = id ?: Identifier.macro(itemName.lowercase().replace(" ", "_").replace("'", ""))
}
