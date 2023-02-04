package space.maxus.macrocosm.fishing

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.common.collect.Multimap
import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.item.runes.RuneType
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.math.Chance
import space.maxus.macrocosm.util.multimap
import space.maxus.macrocosm.area.Area
import java.util.function.Predicate

data class CatchConditions(
    val description: String,
    val predicate: Predicate<Pair<MacrocosmPlayer, Area>>,
    val chance: Float
)

class TrophyFish(
    private val baseName: String,
    private val headSkin: String,
    val conditions: CatchConditions,
    override var rarity: Rarity,
    var tier: TrophyTier? = null
) : MacrocosmItem, Chance {
    override var stats: Statistics = Statistics.zero()
    override var specialStats: SpecialStatistics = SpecialStatistics()
    override var amount: Int = 1
    override var stars: Int = 0
    override val maxStars: Int = 0
    override val id: Identifier get() = id(baseName.lowercase())
    override val type: ItemType = ItemType.OTHER
    override var name: Component
        get() = text("$baseName <bold>${tier?.name}")
        set(@Suppress("UNUSED_PARAMETER") value) {
            // this item should not have name set externally
        }
    override val base: Material = Material.PLAYER_HEAD
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<RegistryPointer> = mutableListOf()
    override val enchantments: HashMap<Identifier, Int> = hashMapOf()
    override val runes: Multimap<RuneSlot, RuneState> = multimap()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0
    override var dye: Dye? = null
    override var skin: SkullSkin? = null

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        val reduced = conditions.description.reduceToList(25).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlank() }
        lore.addAll(reduced)
    }

    override fun addPotatoBooks(amount: Int) {
        // this item should not have potato books applied
    }

    override fun addRune(index: Int, rune: RuneType, tier: Int): Boolean {
        return false
    }

    override fun reforge(ref: Reforge) {
        // this item should not be reforged
    }

    override fun stats(player: MacrocosmPlayer?): Statistics {
        return Statistics.zero()
    }

    override fun unlockRune(index: Int): Boolean {
        return false
    }

    override fun specialStats(): SpecialStatistics {
        return SpecialStatistics()
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        return false
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", headSkin))
        skull.playerProfile = profile
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        val tier = CompoundTag()
        tier.putString("Display", this.tier!!.name)
        tier.putString("Modifier", this.tier!!.modifier)
        tier.putDouble("Chance", this.tier!!.chance)
        cmp.put("Tier", tier)
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as TrophyFish
        val tt = nbt.getCompound("Tier")
        base.tier = TrophyTier(tt.getString("Display"), tt.getString("Modifier"), tt.getDouble("Chance"))
        return base
    }

    override fun clone(): MacrocosmItem {
        return TrophyFish(baseName, headSkin, conditions, rarity)
    }

    override val chance: Double
        get() = conditions.chance.toDouble()
}
