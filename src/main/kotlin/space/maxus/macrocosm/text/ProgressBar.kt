package space.maxus.macrocosm.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import kotlin.math.floor
import kotlin.math.roundToInt

interface ProgressBarTheme {
    val mainColor: TextColor
    val secondaryColor: TextColor
    val tertiaryColor: TextColor get() = mainColor
    val emptyColor: TextColor

    private data class ProgressBarThemeImpl(
        override val mainColor: TextColor,
        override val secondaryColor: TextColor,
        override val emptyColor: TextColor,
        override val tertiaryColor: TextColor = secondaryColor
    ) : ProgressBarTheme

    companion object {
        val FOREST: ProgressBarTheme = ProgressBarThemeImpl(
            NamedTextColor.DARK_GREEN,
            NamedTextColor.GREEN,
            NamedTextColor.WHITE
        )

        val HILLS: ProgressBarTheme = ProgressBarThemeImpl(
            NamedTextColor.DARK_GREEN,
            NamedTextColor.YELLOW,
            NamedTextColor.WHITE,
            NamedTextColor.GOLD
        )

        val SUNLIGHT: ProgressBarTheme = ProgressBarThemeImpl(
            NamedTextColor.YELLOW,
            NamedTextColor.GOLD,
            NamedTextColor.WHITE
        )

        val MYSTERIOUS: ProgressBarTheme = ProgressBarThemeImpl(
            NamedTextColor.DARK_PURPLE,
            NamedTextColor.LIGHT_PURPLE,
            NamedTextColor.DARK_GRAY,
        )
    }
}

class ProgressBar internal constructor(
    private val theme: ProgressBarTheme,
    private val filled: Int,
    private val empty: Int,
    private val showCount: Boolean = false,
    private val scale: Int = 25
) {
    fun scale(newScale: Int): ProgressBar {
        return ProgressBar(theme, filled, empty, showCount, newScale)
    }

    fun showCount(show: Boolean): ProgressBar {
        return ProgressBar(theme, filled, empty, show, scale)
    }

    fun render(): Component {
        return text(toString())
    }

    override fun toString(): String {
        val ratio = filled.toFloat() / (filled + empty)
        val toFill = floor(ratio * scale).roundToInt()
        val toLeaveEmpty = scale - toFill
        return "<strikethrough><${theme.mainColor.asHexString()}>${DASH.repeat(toFill)}<${theme.emptyColor.asHexString()}>${
            DASH.repeat(
                toLeaveEmpty
            )
        }</strikethrough>${if (showCount) " <${theme.secondaryColor.asHexString()}>${filled}<${theme.tertiaryColor.asHexString()}>/<${theme.secondaryColor.asHexString()}>${filled + empty}" else ""}"
    }

    companion object {
        const val DASH = "⏤"

        fun ratio(ratio: Float, total: Int, scale: Int, theme: ProgressBarTheme = ProgressBarTheme.HILLS): ProgressBar {
            val full = floor(ratio * scale).roundToInt()
            return ProgressBar(theme, full, total - full, scale = scale)
        }
    }
}

fun progressBar(
    full: Int,
    total: Int,
    scale: Int = 25,
    theme: ProgressBarTheme = ProgressBarTheme.HILLS,
    showCount: Boolean = false
) = ProgressBar(theme, full, total - full, showCount, scale)

fun progressBar(
    ratio: Float,
    total: Int,
    scale: Int = 25,
    theme: ProgressBarTheme = ProgressBarTheme.HILLS,
    showCount: Boolean = false
) = ProgressBar.ratio(ratio, total, scale, theme).showCount(showCount)
