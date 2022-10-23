package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

open class SkullAbilityItem(
    type: ItemType,
    itemName: String,
    rarity: Rarity,
    val skullOwner: String,
    stats: Statistics,
    abilities: MutableList<MacrocosmAbility> = mutableListOf(),
    specialStats: SpecialStatistics = SpecialStatistics(),
    breakingPower: Int = 0,
    runeTypes: List<Identifier> = listOf(),
    description: String? = null,
    id: Identifier = Identifier.macro(itemName.lowercase().replace(" ", "_").replace("'", "")),
    metaModifier: (ItemMeta) -> Unit = { }
) : AbilityItem(
    type,
    itemName,
    rarity,
    Material.PLAYER_HEAD,
    stats,
    abilities,
    specialStats,
    breakingPower,
    runeTypes.map { RuneSlot.fromId(it) },
    description,
    id = id,
    metaModifier
) {


    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putByte("BlockClicks", 1)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", skullOwner))
        skull.playerProfile = profile
    }

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val item = SkullAbilityItem(
            type,
            itemName,
            rarity,
            skullOwner,
            stats.clone(),
            abilities.map { it.get<MacrocosmAbility>() }.toMutableList(),
            specialStats.clone(),
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
}
