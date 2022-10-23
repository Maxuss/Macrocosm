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
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id

class ColoredEntityArmor(override val base: Material, private var color: Int) :
    AbstractMacrocosmItem(id("colored_${base.name.lowercase()}"), ItemType.OTHER) {
    override var name: Component = text("")
    override var rarity: Rarity = Rarity.SPECIAL
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

class SkullEntityHead(private var eskin: String) : AbstractMacrocosmItem(id("skull_entity_head"), ItemType.OTHER) {
    override var name: Component = text("null")
    override val base: Material = Material.PLAYER_HEAD
    override var rarity: Rarity = Rarity.SPECIAL

    override fun addPotatoBooks(amount: Int) {

    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putString("HeadSkin", eskin)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
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
