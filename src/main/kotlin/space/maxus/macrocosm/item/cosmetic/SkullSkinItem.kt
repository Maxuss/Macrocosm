package space.maxus.macrocosm.item.cosmetic

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.common.collect.Multimap
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.isBlankOrEmpty
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
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.multimap

class SkullSkinItem(val sskin: SkullSkin) : MacrocosmItem {
    override var stats: Statistics = Statistics.zero()
    override var specialStats: SpecialStatistics = SpecialStatistics()
    override var amount: Int = 1
    override var stars: Int = 0
    override val id: Identifier = space.maxus.macrocosm.util.generic.id(
        "${MiniMessage.miniMessage().stripTags(sskin.name).lowercase().replace(" ", "_")}_skin"
    )
    override val type: ItemType = ItemType.OTHER
    override var name: Component = text("${sskin.name} Skin")
    override val base: Material = Material.PLAYER_HEAD
    override var rarity: Rarity = sskin.rarity
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
        lore.add(0, "".toComponent())
        lore.add(0, text("<dark_gray>${if (sskin.isHelmet) "Helmet" else "Pet"} Skin").noitalic())

        val str = if (sskin.isHelmet) {
            "Apply helmet skins to helmets on an <yellow>Anvil<gray> to give your helmet a <light_purple>New & Refreshing<gray> look!"
        } else {
            "Apply pet skins to pets on an <yellow>Anvil<gray> to give your pet a <light_purple>New & Refreshing<gray> look!"
        }
        val reduced = str.reduceToList(25).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        reduced.add("".toComponent())
        lore.addAll(reduced)
        val cmp = if (sskin.isHelmet) {
            val i = Registry.ITEM.find(sskin.target)
            text(
                "<gray>Applicable to <${i.rarity.color.asHexString()}>${MiniMessage.miniMessage().serialize(i.name)}."
            )
        } else {
            val p = Registry.PET.find(sskin.target)
            text(
                "<gray>Applicable to <gold>${p.name} Pet<gray>."
            )
        }
        lore.add(cmp.noitalic())
        lore.add("".toComponent())
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", sskin.skin))
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
        return SkullSkinItem(sskin)
    }
}
