package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import java.util.*

class ColoredEntityArmor(override val base: Material, private var color: Int) : MacrocosmItem {
    override var stats: Statistics get() = Statistics.zero(); set(_) {}
    override var specialStats: SpecialStatistics get() = SpecialStatistics(); set(_) {}
    override var amount: Int = 1
    override var stars: Int = 0
    override val id: Identifier = space.maxus.macrocosm.util.id("colored_${base.name.lowercase()}")
    override val type: ItemType = ItemType.OTHER
    override var name: Component = text("")
    override var rarity: Rarity = Rarity.SPECIAL
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<MacrocosmAbility> = mutableListOf()
    override val enchantments: HashMap<Enchantment, Int> = hashMapOf()
    override val runes: HashMap<ApplicableRune, RuneState> = HashMap()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0
    override var dye: Dye? = null
    override var skin: SkullSkin? = null

    override fun addPotatoBooks(amount: Int) {

    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putInt("LeatherColor", color)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val leather = meta as LeatherArmorMeta
        leather.setColor(Color.fromRGB(this.color))
    }

    override fun stats(player: MacrocosmPlayer?): Statistics {
        return Statistics.zero()
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        if (!enchantment.levels.contains(level))
            return false
        enchantUnsafe(enchantment, level)
        return true
    }

    override fun reforge(ref: Reforge) {

    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as ColoredEntityArmor
        val color = nbt.getInt("LeatherColor")
        base.color = color
        return base
    }

    override fun clone(): MacrocosmItem {
        return ColoredEntityArmor(base, 0x000000)
    }
}

class SkullEntityHead(private var eskin: String) : MacrocosmItem {
    override var stats: Statistics get() = Statistics.zero(); set(_) {}
    override var specialStats: SpecialStatistics get() = SpecialStatistics(); set(_) {}
    override var amount: Int = 1
    override var stars: Int = 0
    override val id: Identifier = space.maxus.macrocosm.util.id("skull_entity_head")
    override val type: ItemType = ItemType.OTHER
    override var name: Component = text("null")
    override val base: Material = Material.PLAYER_HEAD
    override var rarity: Rarity = Rarity.SPECIAL
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override val abilities: MutableList<MacrocosmAbility> = mutableListOf()
    override val enchantments: HashMap<Enchantment, Int> = hashMapOf()
    override val runes: HashMap<ApplicableRune, RuneState> = HashMap()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0
    override var dye: Dye? = null
    override var skin: SkullSkin? = null

    override fun addPotatoBooks(amount: Int) {

    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putString("HeadSkin", eskin)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(UUID.randomUUID())
        profile.setProperty(ProfileProperty("textures", eskin))
        skull.playerProfile = profile
    }

    override fun stats(player: MacrocosmPlayer?): Statistics {
        return Statistics.zero()
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        if (!enchantment.levels.contains(level))
            return false
        enchantUnsafe(enchantment, level)
        return true
    }

    override fun reforge(ref: Reforge) {

    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as SkullEntityHead
        val ski = nbt.getString("HeadSkin")
        base.eskin = ski
        return base
    }

    override fun clone(): MacrocosmItem {
        return SkullEntityHead("null")
    }
}
