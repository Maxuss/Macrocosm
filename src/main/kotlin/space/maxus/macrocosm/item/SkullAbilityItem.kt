package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
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
    description,
    metaModifier = { meta ->
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID())
        profile.setProperty(ProfileProperty("textures", skullOwner))
        skull.playerProfile = profile
        println("APPLIED EXTRA META")
    }
) {
    override fun addExtraMeta(meta: ItemMeta) {
    }
}
