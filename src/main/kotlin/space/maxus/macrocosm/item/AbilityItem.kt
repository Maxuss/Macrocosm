package space.maxus.macrocosm.item

import com.google.common.collect.Multimap
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.registry.registryPointer
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.annotations.PreviewFeature
import space.maxus.macrocosm.util.multimap

open class AbilityItem(
    override val type: ItemType,
    protected val itemName: String,
    override var rarity: Rarity,
    override val base: Material,
    override var stats: Statistics,
    abilities: List<MacrocosmAbility> = listOf(),
    override var specialStats: SpecialStatistics = SpecialStatistics(),
    override var breakingPower: Int = 0,
    runeTypes: List<RuneSlot> = listOf(),
    protected val description: String? = null,
    id: Identifier? = null,
    protected val metaModifier: (ItemMeta) -> Unit = { },
) : AbstractMacrocosmItem(id ?: Identifier.macro(itemName.lowercase().replace(" ", "_").replace("'", "")), type) {
    override val abilities: MutableList<RegistryPointer> by lazy {
        abilities.map { registryPointer(space.maxus.macrocosm.util.general.id("ability"), it) }.toMutableList()
    }

    @Suppress("ClassName")
    object PLACEHOLDER_ITEM : AbilityItem(
        ItemType.OTHER,
        "Placeholder Item",
        Rarity.UNOBTAINABLE,
        Material.DIAMOND_HORSE_ARMOR,
        stats { health = -1f },
        mutableListOf(),
        SpecialStatistics(),
        3,
        listOf()
    )

    override var stars: Int = 0
        set(value) {
            if (value > maxStars)
                return
            else field = value
        }
    override var name: Component = text(itemName)
    override val runes: Multimap<RuneSlot, RuneState> = multimap<RuneSlot, RuneState>().apply {
        for (ty in runeTypes) {
            put(ty, RuneState.EMPTY)
        }
    }

    @PreviewFeature
    override var isDungeonised: Boolean = false

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        super.buildLore(player, lore)
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
            abilities.mapNotNull { it.get() },
            specialStats.clone(),
            metaModifier = metaModifier,
            description = description,
            id = id
        )
        item.enchantments = enchantments.clone() as HashMap<Identifier, Int>
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

    @OptIn(PreviewFeature::class)
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + itemName.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + stats.hashCode()
        result = 31 * result + specialStats.hashCode()
        result = 31 * result + breakingPower
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + metaModifier.hashCode()
        result = 31 * result + abilities.hashCode()
        result = 31 * result + stars
        result = 31 * result + name.hashCode()
        result = 31 * result + runes.hashCode()
        result = 31 * result + isDungeonised.hashCode()
        return result
    }
}
