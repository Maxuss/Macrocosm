package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import java.util.*

class ReforgeStone(
    private val reforgeType: Reforge,
    private val stoneName: String,
    override var rarity: Rarity,
    private val headSkin: String
) : MacrocosmItem {
    override var stats: Statistics = Statistics.zero()
    override var specialStats: SpecialStatistics = SpecialStatistics()
    override var amount: Int = 1
    override var stars: Int = 0
    override val id: Identifier = space.maxus.macrocosm.util.id(stoneName.lowercase().replace(" ", "_"))
    override val type: ItemType = ItemType.OTHER
    override var name: Component = comp(stoneName)
    override val base: Material = Material.PLAYER_HEAD
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<ItemAbility> = mutableListOf()
    override val enchantments: HashMap<Enchantment, Int> = hashMapOf()
    override val runes: HashMap<ApplicableRune, RuneState> = hashMapOf()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0
    override val maxStars: Int = 0

    override fun buildLore(lore: MutableList<Component>) {
        lore.add(0, "".toComponent())
        lore.add(0, comp("<dark_gray>Reforge Stone").noitalic())

        val it = reforgeType.applicable.first()
        val itStr =
            if (it.armor) "Armor" else if (it == ItemType.BOW) "Bows" else if (it.weapon) "Weapons" else if (it.tool) "Tools" else it.name.capitalized() + "s"
        val str =
            "Can be used on a <yellow>Reforge Anvil<gray> to apply the <gold>${reforgeType.name}<gray> reforge to <blue>${itStr}<gray>."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        reduced.add("".toComponent())
        reduced.add(comp("<blue>${reforgeType.name} <gray>(<gold>Legendary<gray>):").noitalic())
        val stats = reforgeType.stats(Rarity.LEGENDARY)
        reduced.addAll(stats.formatSimple())
        reduced.add("".toComponent())
        if (reforgeType.abilityName != null) {
            reduced.add(comp("<blue>Special Ability: <gold><bold><obfuscated>${reforgeType.abilityName}").noitalic())
            reduced.add("".toComponent())
        }
        lore.addAll(reduced)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID())
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

    override fun stats(): Statistics {
        return Statistics.zero()
    }

    override fun specialStats(): SpecialStatistics {
        return SpecialStatistics()
    }

    override fun clone(): MacrocosmItem {
        return ReforgeStone(reforgeType, stoneName, rarity, headSkin)
    }
}
