package space.maxus.macrocosm.accessory.power

import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.accessory.AccessoryBag
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.AbstractMacrocosmItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

private val roundMpValues = listOf(100, 250, 500, 750, 1000)

/**
 * A power stone item, containing an accessory item
 */
class PowerStone(
    id: Identifier,
    name: String,
    val powerId: Identifier,
    override var rarity: Rarity,
    private val headSkin: String,
    protected var uuid: UUID = UUID.randomUUID()
) : AbstractMacrocosmItem(id, ItemType.OTHER) {
    override val base: Material = Material.PLAYER_HEAD
    override var name: Component = text(name)

    override fun addPotatoBooks(amount: Int) {
        // should not be modified
    }

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        val buffer = mutableListOf<String>()
        val power = Registry.ACCESSORY_POWER.findOrNull(powerId) as? StoneAccessoryPower ?: return

        buffer.add("<dark_gray>Power Stone")
        buffer.add("Combine <green>9x<gray> of this stone at the")
        buffer.add("<gold>Thaumaturgist<gray> to permanently")
        buffer.add("unlock the <green>${power.name} power.")
        buffer.add("")
        val mp = player?.accessoryBag?.magicPower ?: 500
        val closestMagicalPower = roundMpValues.minBy { abs(mp - it) }
        buffer.add("At <gold>$closestMagicalPower Magic Power<gray>:")
        val modifier = AccessoryBag.statModifier(closestMagicalPower)
        val stats = power.stats.clone()
        stats.multiply(modifier.toFloat())
        for ((stat, value) in stats.iter()) {
            if (value != 0f) {
                val approx = floor(value).roundToInt()
                val mod = if (value < 0f) "" else "+"
                buffer.add("<${stat.color.asHexString()}>$mod$approx${stat.display}")
            }
        }
        buffer.add("")
        buffer.add("Unique Power Bonus:")
        for (part in power.specialBonus.split("<br>")) {
            buffer.add(part)
        }
        buffer.add("")
        buffer.add("Requires <green>Combat Skill Level ${roman(power.combatLevel)}<gray>!")
        buffer.add("")
        lore.addAll(buffer.map { text("<gray>$it").noitalic() })
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", headSkin))
        skull.playerProfile = profile
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        super.addExtraNbt(cmp)
        cmp.putUUID("UUID", uuid)
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val self = super.convert(from, nbt) as PowerStone
        self.uuid = nbt.getUUID("UUID")
        return self
    }

    override fun clone(): MacrocosmItem {
        return PowerStone(id, name.str(), powerId, rarity, headSkin, UUID.randomUUID())
    }
}
