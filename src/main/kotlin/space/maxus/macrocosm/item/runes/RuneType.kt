package space.maxus.macrocosm.item.runes

import net.axay.kspigot.event.listen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextColor.color
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.buffs.BuffRegistry
import space.maxus.macrocosm.item.runes.types.FlameboundRune
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id

interface RuneSlot {
    val id: Identifier
    val display: String

    fun accepts(rune: RuneType): Boolean

    companion object {
        val COMBAT: RuneSlot get() = DefaultRuneSlot(id("combat"), "⚔") { rune -> rune.spec.isCombat }
        val GATHERING: RuneSlot get() = DefaultRuneSlot(id("gathering"), "♣") { rune -> rune.spec.isGathering }
        val UTILITY: RuneSlot get() = DefaultRuneSlot(id("utility"), "☯") { rune -> rune.spec.isUtility }

        fun specific(spec: RuneSpec): RuneSlot =
            DefaultRuneSlot(id(spec.name.lowercase()), spec.display) { rune -> rune.spec == spec }

        fun typeBound(rune: RuneType): RuneSlot =
            DefaultRuneSlot(rune.id, rune.display) { newRune -> newRune.id == rune.id }

        fun fromId(id: Identifier): RuneSlot {
            return when (val p = id.path) {
                "combat" -> COMBAT
                "gathering" -> GATHERING
                "utility" -> UTILITY
                else -> {
                    if (RuneSpec.values().map { it.name.lowercase() }.contains(p))
                        return specific(RuneSpec.valueOf(p.uppercase()))
                    return typeBound(BuffRegistry.findRune(id))
                }
            }
        }
    }

    private data class DefaultRuneSlot(
        override val id: Identifier,
        override val display: String,
        private val accepts: (RuneType) -> Boolean
    ) : RuneSlot {
        override fun accepts(rune: RuneType): Boolean {
            return this.accepts.invoke(rune)
        }
    }

    fun render(): Component = text("<dark_gray>[$display<dark_gray>]").noitalic()
}

enum class RuneSpec(val display: String) {
    // combat
    OFFENSIVE("\uD83D\uDDE1"),
    DEFENSIVE("\uD83D\uDEE1"),

    // gathering
    MINING("⛏"),
    FISHING("\uD83C\uDFA3"),
    FORAGING("\uD83E\uDE93"),
    EXCAVATING("\uD800\uDF49"),

    // utility
    UTILITY("⌛")
    ;

    val isCombat
        get() = when (this) {
            OFFENSIVE, DEFENSIVE -> true
            else -> false
        }

    val isGathering
        get() = when (this) {
            OFFENSIVE, DEFENSIVE, UTILITY -> false
            else -> true
        }

    val isUtility get() = this == UTILITY
}

data class RuneState(val applied: Identifier?, val tier: Int) {
    companion object {
        val EMPTY = RuneState(null, -1)
    }
}

interface RuneType {
    val id: Identifier
    val spec: RuneSpec
    val display: String
    val color: TextColor
    val headSkin: String

    fun register()
    fun descript(): String

    fun runeTier(item: MacrocosmItem): Int {
        return item.runes.entries().filter { (slot, state) -> slot.accepts(this) && state.applied != null }
            .sumOf { (_, state) -> state.tier }
    }

    fun render(tier: Int): Component {
        val color = colorFromTier(tier).asHexString()
        return text("<$color>[${this.color.asHexString()}$display<$color>]")
    }
}

private fun colorFromTier(tier: Int): TextColor {
    return when (tier) {
        1 -> NamedTextColor.WHITE
        2 -> NamedTextColor.GREEN
        3 -> NamedTextColor.BLUE
        4 -> NamedTextColor.DARK_PURPLE
        5 -> NamedTextColor.GOLD
        else -> NamedTextColor.LIGHT_PURPLE
    }
}

enum class SpecialRunes(val rune: RuneType) {
    FLAMEBOUND(FlameboundRune)
    ;

    companion object {
        fun init() {
            for (v in values()) {
                BuffRegistry.registerRune(id(v.name.lowercase()), v.rune)
            }
        }
    }
}

enum class StatRune(
    override val spec: RuneSpec,
    override val display: String,
    override val color: TextColor,
    val baseStats: Statistics,
    private val modifiedStats: String,
    override val headSkin: String
) : RuneType {
    EMERALD(
        RuneSpec.OFFENSIVE,
        "◎",
        color(0x20FB58),
        stats { critDamage = 3f },
        Statistic.CRIT_DAMAGE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDNhMWFkNGZjYzQyZmI2M2M2ODEzMjhlNDJkNjNjODNjYTE5M2IzMzNhZjJhNDI2NzI4YTI1YThjYzYwMDY5MiJ9fX0="
    ),

    DIAMOND(
        RuneSpec.OFFENSIVE,
        "◆",
        color(0x50B6CC),
        stats { intelligence = 5f },
        Statistic.INTELLIGENCE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjNTdjNzVhZGYzOWVjNmYwZTA5MTYwNDlkZDk2NzFlOThhOGExZTYwMDEwNGU4NGU2NDVjOTg4OTUwYmQ3In19fQ==",
    ),

    AMETHYST(
        RuneSpec.DEFENSIVE,
        "▒",
        color(0x9950CC),
        stats { defense = 5f },
        Statistic.DEFENSE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQ0ODBlMzllYTYzZTM0N2QyNjhkZTgzMDkwZDA5OTg0YmYzNDM5NDExODg0ODM0OGJmNGViNTc0OTBjZTlkMiJ9fX0="
    ),

    REDSTONE(
        RuneSpec.DEFENSIVE,
        "♥",
        color(0xCC6350),
        stats { health = 5f },
        Statistic.HEALTH.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODdhN2E4OTQwNTdkNGExZmYyMmExNjFkNzY2MDBmNzE5ZGE1NzkxNjYzM2Y2ODM4MDhjZjRkMzU4YmI3M2EyMSJ9fX0="
    ),

    STEELWOOD(
        RuneSpec.FORAGING,
        "⚑",
        color(0xEABD77),
        stats { foragingFortune = 2f },
        Statistic.FORAGING_FORTUNE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVmNDg2MWFhNWIyMmVlMjhhOTBlNzVkYWI0NWQyMjFlZmQxNGMwYjFlY2M4ZWU5OThmYjY3ZTQzYmI4ZjNkZSJ9fX0="
    ),

    MOONSTONE(
        RuneSpec.EXCAVATING,
        "Ỿ",
        color(0xFFEEB8),
        stats { excavatingFortune = 2f },
        Statistic.EXCAVATING_FORTUNE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQ2MjBlNGUzZDNhYmZlZDZhZDgxYTU4YTU2YmNkMDg1ZDllOWVmYzgwM2NhYmIyMWZhNmM5ZTM5NjllMmQyZSJ9fX0=",
    ),

    MITHRIL(
        RuneSpec.MINING,
        "ᛯ",
        color(0x70EFBC),
        stats { miningFortune = 2f },
        Statistic.MINING_FORTUNE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzczOGI4YWY4ZDdjZTFhMjZkYzZkNDAxODBiMzU4OTQwM2UxMWVmMzZhNjZkN2M0NTkwMDM3NzMyODI5NTQyZSJ9fX0="
    ),

    AQUAMARINE(
        RuneSpec.FISHING,
        "☔",
        color(0x2485EC),
        stats { seaCreatureChance = .5f },
        Statistic.SEA_CREATURE_CHANCE.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTc3YzFmYzkzMjE2ZTk2ZDQzNWNmOTYyZTExNzNkZThkMWEyNDliNjQ0ODk0ZDcyNjc2ZWJhNzMyZmNkNTZlNyJ9fX0="
    ),

    ADAMANTITE(
        RuneSpec.UTILITY,
        "⌛",
        color(0xFB7420),
        stats { miningSpeed = 10f; strength = 1f },
        "${Statistic.MINING_SPEED.display}<gray> and ${Statistic.STRENGTH.display}",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTAyNjc3MDUzZGM1NDI0NWRhYzRiMzk5ZDE0YWFlMjFlZTcxYTAxMGJkOWMzMzZjOGVjZWUxYTBkYmU4ZjU4YiJ9fX0="
    ),

    SILVER(
        RuneSpec.UTILITY,
        "∞",
        color(0xF6FDFA),
        stats { speed = 3f },
        Statistic.SPEED.display,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTlmZmFjZWM2ZWU1YTIzZDljYjI0YTJmZTlkYzE1YjI0NDg4ZjVmNzEwMDY5MjQ1NjBiZjEyMTQ4NDIxYWU2ZCJ9fX0="
    )

    ;

    override val id: Identifier = id(name.lowercase())

    override fun register() {
        listen<ItemCalculateStatsEvent> { e ->
            val tier = runeTier(e.item)
            if (tier <= 0)
                return@listen

            e.stats.increase(baseStats.clone().apply { multiply(tier.toFloat()) })
        }
    }

    override fun descript(): String {
        return "It is said, that when <yellow>prepared<gray> and <yellow>applied<gray> properly, this rune will boost the wearer's ${modifiedStats}<gray>."
    }

    companion object {
        fun init() {
            for (value in values()) {
                BuffRegistry.registerRune(value.id, value)
            }
            SpecialRunes.init()
        }
    }
}
