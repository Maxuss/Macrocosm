package space.maxus.macrocosm.item.runes

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.common.collect.Multimap
import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.multimap

fun rarityToRuneTier(rarity: Rarity): String {
    return when (rarity) {
        Rarity.COMMON -> "Dusty"
        Rarity.UNCOMMON -> "Fine"
        Rarity.RARE -> "Grand"
        Rarity.EPIC -> "Fabulous"
        Rarity.LEGENDARY -> "Heavenly"
        else -> "Celestial"
    }
}

class RuneItem(
    private val runeType: RuneType,
    private val runeName: String,
    override var rarity: Rarity,
    private val headSkin: String
) : MacrocosmItem {
    override var stats: Statistics = Statistics.zero()
    override var specialStats: SpecialStatistics = SpecialStatistics()
    override var amount: Int = 1
    override var stars: Int = 0
    override val id: Identifier = space.maxus.macrocosm.util.generic.id(
        "${runeType.id.path}_rune_${rarity.name.lowercase()}"
    )
    override val type: ItemType = ItemType.OTHER
    override var name: Component = text(runeName)
    override val base: Material = Material.PLAYER_HEAD
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<MacrocosmAbility> = mutableListOf()
    override val enchantments: HashMap<Enchantment, Int> = hashMapOf()
    override val runes: Multimap<RuneSlot, RuneState> = multimap()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0
    override var dye: Dye? = null
    override var skin: SkullSkin? = null
    override val maxStars: Int = 0

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        lore.add(text("<dark_gray>${runeType.spec.name.capitalized()} Rune").noitalic())
        lore.add("".toComponent())

        val str = runeType.descript()
        for (d in str.reduceToList(35)) {
            lore.add(text("<gray>$d</gray>").noitalic())
        }
        lore.add("".toComponent())
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", headSkin))
        skull.playerProfile = profile
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putByte("BlockClicks", 0)
    }

    override fun addPotatoBooks(amount: Int) {

    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        return false
    }

    override fun reforge(ref: Reforge) {

    }

    override fun stats(player: MacrocosmPlayer?): Statistics {
        return Statistics.zero()
    }

    override fun specialStats(): SpecialStatistics {
        return SpecialStatistics()
    }

    override fun clone(): MacrocosmItem {
        return RuneItem(runeType, runeName, rarity, headSkin)
    }
}
