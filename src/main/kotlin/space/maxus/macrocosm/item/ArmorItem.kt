package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.generic.id

open class ArmorItem(
    val baseName: String,
    val baseId: String,
    protected val baseMaterial: String,
    protected val baseRarity: Rarity,
    protected val baseStats: Statistics = Statistics.zero(),
    protected val baseSpecials: SpecialStatistics = SpecialStatistics(),
    protected val abilities: List<MacrocosmAbility> = listOf(),
    protected val headMeta: (ItemMeta) -> Unit = { },
    protected val chestMeta: (ItemMeta) -> Unit = { },
    protected val legsMeta: (ItemMeta) -> Unit = { },
    protected val bootMeta: (ItemMeta) -> Unit = { },
    protected val commonMeta: (ItemMeta) -> Unit = { },
    protected val runes: List<RuneSlot> = listOf(),
    protected val headSkin: String? = null
) {
    companion object {
        const val HELMET_MODIFIER = .6f
        const val CHESTPLATE_MODIFIER = 1f
        const val LEGGINGS_MODIFIER = .8f
        const val BOOT_MODIFIER = .5f
    }

    open fun helmet(): MacrocosmItem {
        val cached = Registry.ITEM.findOrNull(id("${baseId}_helmet"))
        if (cached != null)
            return cached

        val statClone = baseStats.clone()
        statClone.multiply(HELMET_MODIFIER)
        statClone.round()
        val specClone = baseSpecials.clone()
        specClone.multiply(HELMET_MODIFIER)
        specClone.round()

        val item = AbilityItem(
            ItemType.HELMET,
            "$baseName Helmet",
            baseRarity,
            if (headSkin == null) Material.valueOf("${baseMaterial}_HELMET") else Material.PLAYER_HEAD,
            statClone,
            abilities.toMutableList(),
            specClone,
            runeTypes = runes,
        ) {
            if (it is SkullMeta) {
                val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                profile.setProperty(ProfileProperty("textures", headSkin!!))
                it.playerProfile = profile
            }
            headMeta(it)
            commonMeta(it)
        }
        return item
    }

    open fun chestplate(): MacrocosmItem {
        val cached = Registry.ITEM.findOrNull(id("${baseId}_chestplate"))
        if (cached != null)
            return cached

        val statClone = baseStats.clone()
        statClone.multiply(CHESTPLATE_MODIFIER)
        statClone.round()
        val specClone = baseSpecials.clone()
        specClone.multiply(CHESTPLATE_MODIFIER)
        specClone.round()
        return AbilityItem(
            ItemType.CHESTPLATE,
            "$baseName Chestplate",
            baseRarity,
            Material.valueOf("${baseMaterial}_CHESTPLATE"),
            statClone,
            abilities.toMutableList(),
            specClone,
            runeTypes = runes
        ) {
            chestMeta(it)
            commonMeta(it)
        }
    }

    open fun leggings(): MacrocosmItem {
        val cached = Registry.ITEM.findOrNull(id("${baseId}_leggings"))
        if (cached != null)
            return cached

        val statClone = baseStats.clone()
        statClone.multiply(LEGGINGS_MODIFIER)
        statClone.round()
        val specClone = baseSpecials.clone()
        specClone.multiply(LEGGINGS_MODIFIER)
        specClone.round()
        return AbilityItem(
            ItemType.LEGGINGS,
            "$baseName Leggings",
            baseRarity,
            Material.valueOf("${baseMaterial}_LEGGINGS"),
            statClone,
            abilities.toMutableList(),
            specClone,
            runeTypes = runes
        ) {
            legsMeta(it)
            commonMeta(it)
        }
    }

    open fun boots(): MacrocosmItem {
        val cached = Registry.ITEM.findOrNull(id("${baseId}_boots"))
        if (cached != null)
            return cached

        val statClone = baseStats.clone()
        statClone.multiply(BOOT_MODIFIER)
        statClone.round()
        val specClone = baseSpecials.clone()
        specClone.multiply(BOOT_MODIFIER)
        specClone.round()
        return AbilityItem(
            ItemType.BOOTS,
            "$baseName Boots",
            baseRarity,
            Material.valueOf("${baseMaterial}_BOOTS"),
            statClone,
            abilities.toMutableList(),
            specClone,
            runeTypes = runes
        ) {
            bootMeta(it)
            commonMeta(it)
        }
    }

}
