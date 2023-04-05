package space.maxus.macrocosm.pets

import com.destroystokyo.paper.profile.ProfileProperty
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.items.name
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoOwnedPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.progressBar
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import java.io.Serializable
import java.util.*
import kotlin.math.roundToInt

data class StoredPet(
    val id: Identifier,
    var rarity: Rarity,
    var level: Int,
    var overflow: Double,
    var petItem: Identifier = Identifier.NULL,
    var candiesEaten: Int = 0,
    val petId: UUID = UUID.randomUUID(),
    var skin: Identifier = Identifier.NULL
) : Serializable, MongoConvert<MongoOwnedPet> {

    fun menuItem(player: MacrocosmPlayer): ItemStack {
        val base = Registry.PET.find(id)
        val name = text("<gray>[Lvl ${level}] <${rarity.color.asHexString()}>${base.name}").noitalic()
        val lore = mutableListOf<Component>()

        lore.add(text("<dark_gray>${base.preferredSkill.inst.name} Pet").noitalic())
        lore.add("".toComponent())

        for ((stat, amount) in base.stats(level, rarity).iter()) {
            if (amount == 0f)
                continue
            lore.add(text("<gray>$stat: <red>${stat.type.formatSigned(amount, false)?.str()}").noitalic())
        }

        lore.add("".toComponent())

        base.abilitiesForRarity(rarity).forEach {
            lore.addAll(it.description(this))
            lore.add("".toComponent())
        }

        if(petItem.isNotNull()) {
            // TODO: pet item stuff
        }

        if(candiesEaten > 0) {
            lore.add(text("<green>($candiesEaten/10) Pet Candy Used"))
            lore.add(Component.empty())
        }

        if (level >= base.maxLevel) {
            // max level reached
            lore.add(text("<aqua><bold>MAX LEVEL").noitalic())
        } else {
            val next = level + 1
            val table = ProgressivePetTable((rarity.ordinal + 1) / 7.3f)
            val requiredExp = table.expForLevel(level)
            val progressBar = progressBar(overflow.toFloat(), requiredExp.roundToInt(), showCount = true)
            val ratio = (overflow / table.expForLevel(level + 1)).toBigDecimal().setScale(1)

            lore.add(text("<gray>Progress to Level $next: <yellow>${Formatting.withCommas(ratio)}%").noitalic())
            lore.add(progressBar.render())

        }

        lore.add("".toComponent())

        lore.add(text(if (player.activePet?.stored == this) "<red>Click to despawn!" else "<yellow>Click to summon!").noitalic())

        return itemStack(Material.PLAYER_HEAD) {
            meta<SkullMeta> {
                val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                profile.setProperty(
                    ProfileProperty(
                        "textures",
                        if (skin.isNull()) base.headSkin else (Registry.COSMETIC.find(skin) as SkullSkin).skin
                    )
                )
                playerProfile = profile
                this.name = name
                this.lore(lore)
            }
        }
    }

    override val mongo: MongoOwnedPet
        get() = MongoOwnedPet(id.toString(), rarity, level, overflow, skin.toString(), petId, petItem.toString(), candiesEaten)
}
