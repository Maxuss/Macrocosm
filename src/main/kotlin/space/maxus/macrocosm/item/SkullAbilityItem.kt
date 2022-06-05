package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import java.util.*

open class SkullAbilityItem(
    type: ItemType,
    itemName: String,
    rarity: Rarity,
    val skullOwner: String,
    stats: Statistics,
    abilities: MutableList<MacrocosmAbility> = mutableListOf(),
    specialStats: SpecialStatistics = SpecialStatistics(),
    breakingPower: Int = 0,
    applicableRunes: List<ApplicableRune> = listOf(),
    description: String? = null,
    ) : AbilityItem(
    type,
    itemName,
    rarity,
    Material.PLAYER_HEAD,
    stats,
    abilities,
    specialStats,
    breakingPower,
    applicableRunes,
    description
) {
    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putByte("BlockClicks", 1)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID())
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
            abilities,
            specialStats.clone(),
            description = description
        )
        item.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        item.reforge = reforge?.clone()
        item.rarityUpgraded = rarityUpgraded
        item.stars = stars
        item.breakingPower = breakingPower
        item.runes.putAll(runes)
        return item
    }
}
