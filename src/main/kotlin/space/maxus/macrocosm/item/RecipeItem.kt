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
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.putId
import space.maxus.macrocosm.util.stripTags

class RecipeItem(
    override val base: Material,
    override var rarity: Rarity,
    private val baseName: String,
    val headSkin: String? = null,
    private val description: String? = null,
    private val glow: Boolean = false
) : AbstractMacrocosmItem(
    Identifier.macro(baseName.stripTags().replace("'", "").lowercase().replace(" ", "_")),
    ItemType.OTHER
) {
    override var name: Component = text(baseName)

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        if (description != null) {
            val reduced = description.reduceToList(30).map { text("<dark_gray>$it").noitalic() }.toMutableList()
            reduced.removeIf { it.toLegacyString().isBlank() }
            lore.addAll(reduced)
            lore.add("".toComponent())
        }
        lore.add(text("<yellow>Right click to view recipes!").noitalic())
        lore.add("".toComponent())
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putByte("BlockClicks", 0)
        cmp.putId("ViewRecipes", this.id)
    }

    override fun addExtraMeta(meta: ItemMeta) {
        if (glow)
            meta.addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1, true)
        if (base == Material.PLAYER_HEAD && headSkin != null) {
            val skull = meta as SkullMeta
            val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
            profile.setProperty(ProfileProperty("textures", headSkin))
            skull.playerProfile = profile
        }
    }

    override fun clone(): MacrocosmItem {
        return RecipeItem(base, rarity, baseName, headSkin, description, glow)
    }
}
