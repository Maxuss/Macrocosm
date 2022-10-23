package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.item.runes.RuneType
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.getId

class PetItem(
    id: Identifier,
    private var nameStr: String,
    private val headSkin: String,
    var stored: StoredPet? = null
) : AbstractMacrocosmItem(id, ItemType.OTHER) {
    override var name: Component = Component.empty()
    override val base: Material = Material.PLAYER_HEAD
    override var rarity: Rarity = stored?.rarity ?: Rarity.COMMON
    override fun buildName(): Component {
        return text("<gray>[Lvl ${stored!!.level}] <${stored!!.rarity.color.asHexString()}>$nameStr").noitalic()
    }

    override fun addExtraMeta(meta: ItemMeta) {
        val skull = meta as SkullMeta
        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
        profile.setProperty(ProfileProperty("textures", headSkin))
        skull.playerProfile = profile
    }

    override fun addExtraNbt(cmp: CompoundTag) {
        if (stored != null) {
            val st = stored!!
            val pet = CompoundTag()
            pet.putInt("LVL", st.level)
            pet.putDouble("Overflow", st.overflow)
            pet.putInt("Rarity", st.rarity.ordinal)
            cmp.put("Pet", pet)
        }
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as PetItem
        val petTag = nbt.getCompound("Pet")
        val stored = StoredPet(
            nbt.getId("ID"),
            Rarity.values()[petTag.getInt("Rarity")],
            petTag.getInt("LVL"),
            petTag.getDouble("Overflow")
        )
        base.stored = stored
        base.rarity = stored.rarity
        return base
    }

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        val pet = stored!!
        val base = Registry.PET.find(pet.id)

        val newLore = mutableListOf<Component>()
        newLore.add(text("<dark_gray>${base.preferredSkill.inst.name} Pet").noitalic())
        newLore.add("".toComponent())

        for ((stat, amount) in base.stats(pet.level, pet.rarity).iter()) {
            if (amount == 0f)
                continue
            newLore.add(text("<gray>$stat: <red>${stat.type.formatSigned(amount, false)?.str()}").noitalic())
        }

        newLore.add("".toComponent())

        for (cmp in newLore.reversed()) {
            lore.add(0, cmp)
        }

        for (ability in base.abilitiesForRarity(pet.rarity)) {
            lore.addAll(ability.description(pet))
            lore.add("".toComponent())
        }
    }

    override fun enchant(enchantment: Enchantment, level: Int): Boolean {
        return false
    }

    override fun addRune(index: Int, rune: RuneType, tier: Int): Boolean {
        return false
    }

    override fun clone(): MacrocosmItem {
        return PetItem(id, nameStr, headSkin, null)
    }
}
