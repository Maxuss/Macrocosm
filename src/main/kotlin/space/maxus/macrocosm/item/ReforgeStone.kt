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
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text

class ReforgeStone(
    private val reforgeType: Reforge,
    private val stoneName: String,
    override var rarity: Rarity,
    val headSkin: String
) : AbstractMacrocosmItem(
    space.maxus.macrocosm.util.general.id(stoneName.lowercase().replace(" ", "_")),
    ItemType.REFORGE_STONE
) {
    override var name: Component = text(stoneName)
    override val base: Material = Material.PLAYER_HEAD

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        lore.add(0, "".toComponent())
        lore.add(0, text("<dark_gray>Reforge Stone").noitalic())

        val it = reforgeType.applicable.first()
        val itStr =
            if (it.armor) "Armor" else if (it == ItemType.BOW) "Bows" else if (it.weapon) "Weapons" else if (it.tool) "Tools" else it.name.capitalized() + "s"
        val str =
            "Can be used on an <yellow>Anvil<gray> to apply the <gold>${reforgeType.name}<gray> reforge to <blue>${itStr}<gray>."
        val reduced = str.reduceToList(25).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlank() }
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
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", headSkin))
        skull.playerProfile = profile
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putByte("BlockClicks", 0)
    }

    override fun addPotatoBooks(amount: Int) {
        // this item should not have potato books applied
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        return false
    }

    override fun reforge(ref: Reforge) {
        // this item should not be reforged
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
