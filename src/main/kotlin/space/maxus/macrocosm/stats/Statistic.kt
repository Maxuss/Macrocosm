package space.maxus.macrocosm.stats

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.text.text
import java.io.Serializable
import java.util.*
import kotlin.math.roundToInt

enum class Statistic(
    val type: StatisticType,
    val color: TextColor,
    val specialChar: String,
    val default: Float = 0f,
    val percents: Boolean = false,
    val hidden: Boolean = false,
    val specialized: Boolean = false,
    val secret: Boolean = false,
    val displayItem: Material? = null,
    val displaySkin: String? = null,
    val description: String = "",
    val addExtraLore: MutableList<String>.(Statistics) -> Unit = { },
    val hiddenFancy: Boolean = false
) : Serializable {
    DAMAGE(
        StatisticType.OFFENSIVE,
        NamedTextColor.RED,
        "\uD83D\uDDE1",
        10f,
        hiddenFancy = true
    ),
    STRENGTH(
        StatisticType.OFFENSIVE,
        NamedTextColor.RED,
        "\uD83D\uDDE1",
        10f,
        description = "Strength increases your base melee damage, including both punching and weapons",
        displayItem = Material.BLAZE_POWDER
    ),
    FEROCITY(
        StatisticType.OFFENSIVE,
        NamedTextColor.RED,
        "⚡",
        description = "Ferocity grants percent chance to double-strike enemies. Increments of 100 increase the base number of strikes.",
        displayItem = Material.RED_DYE
    ),
    CRIT_CHANCE(
        StatisticType.OFFENSIVE,
        NamedTextColor.BLUE,
        "\uD83D\uDD31",
        10f,
        percents = true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTE2OWM5MGM4ODc0YWI1NzViMjAxYjYxNmE2OWVhYzdlMGI1YWM2OWJiY2NjYmIyNzcyZTM2Nzc2ZmU2OTQ0MSJ9fX0=",
        description = "Crit Chance is your chance to deal extra damage to enemies."
    ),
    CRIT_DAMAGE(
        StatisticType.OFFENSIVE,
        NamedTextColor.BLUE,
        "☠",
        100f,
        true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U0ZjQ5NTM1YTI3NmFhY2M0ZGM4NDEzM2JmZTgxYmU1ZjJhNDc5OWE0YzA0ZDlhNGRkYjcyZDgxOWVjMmIyYiJ9fX0=",
        description = "Crit Damage is the amount of extra damage you deal when landing a critical hit."
    ),
    BONUS_ATTACK_SPEED(
        StatisticType.OFFENSIVE,
        NamedTextColor.YELLOW,
        "⚔",
        percents = true,
        displayItem = Material.GOLDEN_SWORD,
        description = "Bonus Attack Speed decreases the time between hits on your enemy."
    ),
    SEA_CREATURE_CHANCE(
        StatisticType.OFFENSIVE,
        NamedTextColor.DARK_AQUA,
        "⚓",
        20f,
        true,
        displayItem = Material.FISHING_ROD,
        description = "Sea Creature Chance is your chance to catch Sea Creatures while fishing."
    ),
    ABILITY_DAMAGE(
        StatisticType.OFFENSIVE,
        NamedTextColor.RED,
        "⏼",
        0f,
        percents = true,
        secret = true,
        displayItem = Material.BEACON,
        description = "Ability Damage is the percentage of bonus damage applied to your spells."
    ),
    TRUE_DAMAGE(
        StatisticType.OFFENSIVE,
        NamedTextColor.WHITE,
        "☈",
        secret = true,
        displayItem = Material.DIAMOND,
        description = "True Damage is the damage you deal to your enemies that will be ignored by their defense."
    ),

    VITALITY(
        StatisticType.DEFENSIVE,
        NamedTextColor.RED,
        "♩",
        0f,
        displayItem = Material.GLISTERING_MELON_SLICE,
        description = "Vitality is the extra regeneration you get every second."
    ),
    HEALTH(
        StatisticType.DEFENSIVE,
        NamedTextColor.RED,
        "❤",
        100f,
        displayItem = Material.GOLDEN_APPLE,
        description = "Health is the amount of your total maximum health points",
        addExtraLore = {
            val regen = it[HEALTH] / 100f
            add("")
            add("<gray>Regeneration: <red>${Formatting.stats(regen.toBigDecimal())} HP/s")
        }
    ),
    DEFENSE(
        StatisticType.DEFENSIVE,
        NamedTextColor.GREEN,
        "🛡",
        50f,
        displayItem = Material.IRON_CHESTPLATE,
        description = "Defense reduces the damage that you take from enemies.",
        addExtraLore = {
            val red = DamageCalculator.calculateDamageReduction(it)
            val ehp = DamageCalculator.calculateEffectiveHealth(it)
            add("")
            add("<gray>Damage Reduction: <green>${Formatting.stats((red * 100f).toBigDecimal())}%")
            add("<gray>Effective Health: <red>${ehp.roundToInt()} ${HEALTH.specialChar}")
        }
    ),
    TRUE_DEFENSE(
        StatisticType.DEFENSIVE,
        NamedTextColor.WHITE,
        "❂",
        secret = true,
        displayItem = Material.WHITE_DYE,
        description = "True Defense reduces the <white>True Damage<gray> that you take from enemies."
    ),
    SPEED(
        StatisticType.DEFENSIVE,
        NamedTextColor.WHITE,
        "✦",
        100f,
        displayItem = Material.SUGAR,
        description = "Speed increases your walk speed."
    ),
    INTELLIGENCE(
        StatisticType.DEFENSIVE,
        NamedTextColor.AQUA,
        "✎",
        100f,
        displayItem = Material.ENCHANTED_BOOK,
        description = "Intelligence increases both your Mana Pool and the damage of your magical items.",
        addExtraLore = {
            val damageIncrease = it[INTELLIGENCE]
            add("")
            add("<gray>Magic Damage: <aqua>+${damageIncrease.roundToInt()}%")
        }
    ),
    VIGOR(
        StatisticType.DEFENSIVE,
        TextColor.color(0x27C38B),
        "♫",
        0f,
        displayItem = Material.ENCHANTING_TABLE,
        description = "Vigor increases your base Mana Regeneration."
    ),

    MINING_SPEED(
        StatisticType.DEFENSIVE,
        NamedTextColor.YELLOW,
        "⛏",
        100f,
        specialized = true,
        displayItem = Material.DIAMOND_PICKAXE,
        description = "Mining Speed increases the speed of breaking blocks."
    ),
    MINING_FORTUNE(
        StatisticType.DEFENSIVE,
        NamedTextColor.GOLD,
        "♣",
        0f,
        specialized = true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjczNTc5NTc1Y2E4OGIzYThhZmUxZWQxODkwN2IzMTI1ZmUwOTg3YjAyYTg4ZWYwZThhMDEwODdjM2QwMjRjNCJ9fX0=",
        description = "Mining Fortune is the chance to gain double drops from ores, with a chance of triple+ drops at values greater than 100."
    ),
    EXCAVATING_FORTUNE(
        StatisticType.DEFENSIVE,
        NamedTextColor.GOLD,
        "♣",
        0f,
        specialized = true,
        displaySkin = "ewogICJ0aW1lc3RhbXAiIDogMTY1Nzk2MTg0ODAzOSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtX3h1cyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iZjM4OWI0OTVmMTA1YTJiNDgzNzI3NGY3ZjM0NDcyYzZiYTc0MTljOGQ4ZGI0ZDE3MDM1OWM4NDdiMzRlMWFlIgogICAgfQogIH0KfQ==",
        description = "Excavating Fortune is the chance to gain double drops from sands, with a chance of triple+ drops at values greater than 100."
    ),
    FARMING_FORTUNE(
        StatisticType.DEFENSIVE,
        NamedTextColor.GOLD,
        "♣",
        0f,
        specialized = true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjIwZWU3NzQxZmYxYjk1OGRiYjlmYTdjZGRhZDljM2NjZTkzMzczZjQ3MGY5YjgzNGRhMDJkYTY3YzgyMDJhNCJ9fX0=",
        description = "Farming Fortune is the chance to gain double drops from crops, with a chance of triple+ drops at values greater than 100."
    ),
    FORAGING_FORTUNE(
        StatisticType.DEFENSIVE,
        NamedTextColor.GOLD,
        "♣",
        0f,
        specialized = true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0NGUyYThkZmY5MGY1YjAwNWU3NmU2ZjVkYjdjMTJhZTU5Y2JiYzU2ZDhiYzgwNTBmM2UzZGJmMGMzYjczNCJ9fX0=",
        description = "Foraging Fortune is the chance to gain double drops from logs, with a chance of triple+ drops at values greater than 100."
    ),

    TREASURE_CHANCE(
        StatisticType.OFFENSIVE,
        NamedTextColor.DARK_BLUE,
        "\uD83C\uDF0A",
        5f,
        true,
        specialized = true,
        displayItem = Material.PRISMARINE_CRYSTALS,
        description = "Treasure Chance is your chance to catch Treasures and Trophy Fish while fishing."
    ),
    PET_LUCK(
        StatisticType.DEFENSIVE,
        NamedTextColor.LIGHT_PURPLE,
        "♣",
        0f,
        secret = true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGI1MjJmNmQ3N2MwNjk2YzlkMWYyYWQ0OWJmYTNjYjgyMDVhNWU2MjNhZjFjNDIwYmQ3NDBkYzQ3MTkxNGU5NyJ9fX0=",
        description = "Pet Luck gives you higher chance to obtain high rarity pets."
    ),
    MAGIC_FIND(
        StatisticType.DEFENSIVE,
        NamedTextColor.AQUA,
        "✯",
        0f,
        secret = true,
        displaySkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNlZjQ3YmE3OWM3YjU1MGNmMGQ2OTQ2NWM4MmU2ZTcyYjUxNDY4NTk3OGZlNWY3YjZkMTJlMTQyNDAyZmRkZiJ9fX0=",
        description = "Magic Find increases chance of getting rare items."
    ),
    SUMMONING_POWER(
        StatisticType.DEFENSIVE,
        TextColor.color(0x7620D8),
        "☻",
        0f,
        secret = true,
        displaySkin = "ewogICJ0aW1lc3RhbXAiIDogMTY1ODU2NDY1NjI2NSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtX3h1cyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MzJiOTUxNzYxZDE2M2IxY2Y1NmJjYTFmODk2NWQ0NWRmNTdjZDYzMmQ0YjQzNjA4NmFkN2VjYjI1YTlmMzMzIgogICAgfQogIH0KfQ==",
        description = "Summoning Power increases the maximum amount of minions you can have at the moment."
    ),

    DAMAGE_BOOST(StatisticType.OFFENSIVE, NamedTextColor.GOLD, "⚠", 0f, true, true),
    DAMAGE_REDUCTION(StatisticType.DEFENSIVE, NamedTextColor.GOLD, "⓪", 0f, true, true),
    ;

    val display =
        MiniMessage.miniMessage().serialize(Component.text("$specialChar $this").color(color).append(text("<reset>")))

    override fun toString() =
        name.lowercase().split("_")
            .joinToString(separator = " ") { str -> str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

    fun formatSimple(num: Float): Component? {
        if (hidden) return null
        return explicitFormatSimple(num)
    }

    fun formatFancy(num: Float): Component? {
        if (hidden || hiddenFancy) return null
        return explicitFormatFancy(num)
    }

    fun explicitFormatSimple(num: Float): Component? {
        val number = type.formatSigned(num) ?: return null
        val comp = text("<gray>$this: </gray>").append(number)
        return (if (percents) comp.append("%".toComponent().color(type.color)) else comp).noitalic()
    }

    fun explicitFormatFancy(num: Float): Component {
        val comp = text("$specialChar $this ").color(color).append(type.format(num).color(NamedTextColor.WHITE))
        return (if (percents) comp.append("%".toComponent().color(NamedTextColor.WHITE)) else comp).noitalic()
    }
}
