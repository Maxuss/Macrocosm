package space.maxus.macrocosm.item.runes

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.item.buffs.BuffRegistry
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id

interface ApplicableRune {
    val id: Identifier
    val color: TextColor
    val modifiedStats: String

    fun char(): String
    fun locked(): Component
    fun unlocked(): Component
    fun full(level: Int): Component
    fun stats(level: Int): Statistics
}

data class RuneState(val open: Boolean = false, val tier: Int = 0) {
    companion object {
        val ZERO: RuneState = RuneState(false, 0)
    }
}

private fun colorFromTier(tier: Int): TextColor {
    return when (tier) {
        1 -> NamedTextColor.WHITE
        2 -> NamedTextColor.GREEN
        3 -> NamedTextColor.BLUE
        4 -> NamedTextColor.DARK_PURPLE
        5 -> NamedTextColor.GOLD
        else -> NamedTextColor.AQUA
    }
}

enum class DefaultRune(private val char: String, override val color: TextColor, private val baseStats: Statistics,
                       override val modifiedStats: String, val skin: String): ApplicableRune {
    EMERALD(
        "◎",
        TextColor.color(0x20FB58),
        stats { critDamage = 5f },
        Statistic.CRIT_DAMAGE.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMzI1MDczOSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjdkMmU0OTc0YWNkYmI1NjU1MzZlYTg2MWJmNWUyN2YyNzY2ODljZDM0OWVlZjc2MjM4YjBjZWJhYzMyZGI3NSIKICAgIH0KICB9Cn0="
    ),
    DIAMOND(
        "◆",
        TextColor.color(0x50B6CC),
        stats { intelligence = 10f },
        Statistic.INTELLIGENCE.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDE5MTQ4NywKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDFjZjQ4NzdiNTc2NzE4YjAwMTVkZGFiYjhjMGM4NzQ5MGI2ZGQ4NDUwOGRhYzM5N2E4MWQ4ZDU5ODdkYTc4NiIKICAgIH0KICB9Cn0="
    ),
    REDSTONE(
        "☄",
        TextColor.color(0xCC6350),
        stats { health = 10f },
        Statistic.HEALTH.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDE1NjUyNywKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVkNTZhZTg2YjdkYjg1YTg5ZmE2NWUxN2RmYzYxMzM1Y2RjZGM1Mzg0OTBmZDc5MzQ3YmRiMjIwMzU5NjUyNCIKICAgIH0KICB9Cn0="
    ),
    AMETHYST(
        "▒",
        TextColor.color(0x9950CC),
        stats { defense = 15f },
        Statistic.DEFENSE.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDI5MDM2NywKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmFmOTI3ZGNiZTkxMjBiN2QyMjUyOTlhNTgxYWU1YTMyMGNjNTIzODYyZDhkNDE1YjhlNWY4NWIxZWE3ZjQzZiIKICAgIH0KICB9Cn0="
    ),
    SILVER(
        "★",
        TextColor.color(0xF6FDFA),
        stats { critChance = 2f },
        Statistic.CRIT_CHANCE.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDI1MjA0OCwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI2NWZiN2ViMWU0MjQ1OTI3ZDAyZTI2NGQ1YjlkNWQ5YmJmNDJlZjdmYmRiNjI1MDlhNDYxMjllODQ2NjZiZiIKICAgIH0KICB9Cn0="
    ),
    MITHRIL(
        "⚑",
        TextColor.color(0x70EFBC),
        stats { miningSpeed = 10f; miningFortune = 5f },
        "${Statistic.MINING_SPEED.display}<gray> and ${Statistic.MINING_FORTUNE.display}",
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDM0NzUxMiwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzcyOWQwOTZlYTVkNzlmNGFiMzcxYTcwMWU4YTg0NmNiZDRkMzlkZDdjN2Y5NWE2NTA0NDQ2MzIzMDBlZTAxOSIKICAgIH0KICB9Cn0"
    ),
    ADAMANTITE(
        "\uD83D\uDD25",
        TextColor.color(0xFB7420),
        stats { strength = 6f },
        Statistic.STRENGTH.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDAyMTg1MCwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYxN2I5ZjBmNTQzNDYwODRiMjhkMzAyN2I0ZmY3OTA4Y2MzODQ0OGVkOTRmNzNiNzY4NzUyYTQ4YTQxOTczOSIKICAgIH0KICB9Cn0="
    ),
    CRYING_PEARL(
        "∞",
        TextColor.color(0xc595ff),
        stats { attackSpeed = 2.5f },
        Statistic.BONUS_ATTACK_SPEED.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDEzMDc4OCwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzA5OGY1NmVjNjYzZTEzMWY1NmMzOTAxNTVhMGU2ZDY4N2RlYzM5NjBiMmVkYTU2MDcyNTg0YWVjZTE2ZmUwZCIKICAgIH0KICB9Cn0="
    ),
    MOONSTONE(
        "☽",
        TextColor.color(0xFFEEB8),
        stats { intelligence = 5f; abilityDamage = 2.5f },
        "${Statistic.INTELLIGENCE.display}<gray> and ${Statistic.ABILITY_DAMAGE.display}",
        "ewogICJ0aW1lc3RhbXAiIDogMTY1Mzk5OTg4NDk5OCwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ0MWFkOTNjMjk3ZGI0Y2YzOGYwYTZiNjJmMDI0ODFjOGRmMWI3ZTBlMmY1ZGQxZWIwYTEwMjIyNjAzNzA3OCIKICAgIH0KICB9Cn0="
    ),
    PRISMARITE(
        "⚓",
        TextColor.color(0x15C38B),
        stats { seaCreatureChance = .5f },
        Statistic.SEA_CREATURE_CHANCE.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMDM4MzQxNSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhjNGZhMzYwMDA5M2RiMTNhZGM4NTk1OTFmNTBiYjMwZjU2MWRlYjcxYjlmYzljNWJmYWE0OWExYjI2NTMxYSIKICAgIH0KICB9Cn0="
    ),
    CRYSTALLITE(
        "\uD83C\uDFA3",
        TextColor.color(0x2485EC),
        stats { treasureChance = .5f },
        Statistic.TREASURE_CHANCE.display,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1Mzk5OTgyODU2NSwKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDYXRHaXJsTWF4dXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjgzNWE4OTNjMDcwMTA0Nzk4NDZlMGM3YTMxOTAxNzIyMWQzMDc0NTkzOTg3NWMyNDZkMDBjZDA1MmQxNDBjNyIKICAgIH0KICB9Cn0="
    ) //
    ;

    override val id: Identifier = id(name.lowercase())

    override fun char(): String {
        return char
    }

    override fun full(level: Int): Component {
        val tierColor = colorFromTier(level)
        return "[".toComponent().color(tierColor).append(comp(char).color(color))
            .append("]".toComponent().color(tierColor))
    }

    override fun locked(): Component {
        return comp("[$char]").color(NamedTextColor.DARK_GRAY)
    }

    override fun unlocked(): Component {
        return comp("[$char]").color(NamedTextColor.GRAY)
    }

    override fun stats(level: Int): Statistics {
        val clone = baseStats.clone()
        clone.multiply(level.toFloat())
        return clone
    }

    companion object {
        fun init() {
            for (v in values()) {
                BuffRegistry.registerRune(v.id, v)
            }
        }
    }
}
