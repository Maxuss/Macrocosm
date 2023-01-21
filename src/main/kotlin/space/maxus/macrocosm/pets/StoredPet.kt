@file:Suppress("UNNECESSARY_SAFE_CALL")

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
import space.maxus.macrocosm.damage.truncateBigNumber
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoOwnedPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import java.io.Serializable
import java.math.MathContext
import kotlin.math.max
import kotlin.math.roundToInt

data class StoredPet(
    val id: Identifier,
    var rarity: Rarity,
    var level: Int,
    var overflow: Double,
    val skin: Identifier? = null
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

        if (level >= base.maxLevel) {
            // max level reached
            lore.add(text("<aqua><bold>MAX LEVEL").noitalic())
        } else {
            val next = level + 1
            val table = ProgressivePetTable((rarity.ordinal + 1) / 7.3f)
            val requiredExp = table.expForLevel(level)
            val ratio = ((overflow / requiredExp) * 100).toBigDecimal().round(MathContext(1))
            val coloredBarCount = (25f * (overflow / requiredExp)).roundToInt()
            val emptyBarCount = max(0, 25 - coloredBarCount)

            lore.add(text("<gray>Progress to Level $next: <yellow>${Formatting.withCommas(ratio)}%").noitalic())
            lore.add(
                text(
                    "<green>${"-".repeat(coloredBarCount)}<white>${"-".repeat(emptyBarCount)}  <yellow>${
                        Formatting.withCommas(
                            overflow.toBigDecimal()
                        )
                    }<gold>/${truncateBigNumber(requiredExp.toFloat())}"
                ).noitalic()
            )
        }

        lore.add("".toComponent())

        lore.add(text(if (player.activePet?.referring(player) == this) "<red>Click to despawn!" else "<yellow>Click to summon!").noitalic())

        return itemStack(Material.PLAYER_HEAD) {
            meta<SkullMeta> {
                val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                profile.setProperty(
                    ProfileProperty(
                        "textures",
                        if (skin == null) base.headSkin else (Registry.COSMETIC.find(skin) as SkullSkin).skin
                    )
                )
                playerProfile = profile
                this.name = name
                this.lore(lore)
            }
        }
    }

    override val mongo: MongoOwnedPet
        get() = MongoOwnedPet(id.toString(), rarity, level, overflow, id?.toString())
}
