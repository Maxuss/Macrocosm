package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.common.collect.Multimap
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.multimap
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
    override val id: Identifier = space.maxus.macrocosm.util.generic.id(stoneName.lowercase().replace(" ", "_"))
    override val type: ItemType = ItemType.OTHER
    override var name: Component = text(stoneName)
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

    override fun buildLore(lore: MutableList<Component>) {
        lore.add(0, "".toComponent())
        lore.add(0, text("<dark_gray>Reforge Stone").noitalic())

        val it = reforgeType.applicable.first()
        val itStr =
            if (it.armor) "Armor" else if (it == ItemType.BOW) "Bows" else if (it.weapon) "Weapons" else if (it.tool) "Tools" else it.name.capitalized() + "s"
        val str =
            "Can be used on an <yellow>Anvil<gray> to apply the <gold>${reforgeType.name}<gray> reforge to <blue>${itStr}<gray>."
        val reduced = str.reduceToList(25).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        reduced.add("".toComponent())
        reduced.add(text("<blue>${reforgeType.name} <gray>(<gold>Legendary<gray>):").noitalic())
        val stats = reforgeType.stats(Rarity.LEGENDARY)
        reduced.addAll(stats.formatSimple())
        reduced.add("".toComponent())
        if (reforgeType.abilityName != null) {
            reduced.add(text("<blue>Special Ability: <gold><bold><obfuscated>${reforgeType.abilityName}").noitalic())
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

    override fun stats(player: MacrocosmPlayer?): Statistics {
        return Statistics.zero()
    }

    override fun specialStats(): SpecialStatistics {
        return SpecialStatistics()
    }

    override fun clone(): MacrocosmItem {
        return ReforgeStone(reforgeType, stoneName, rarity, headSkin)
    }
}
