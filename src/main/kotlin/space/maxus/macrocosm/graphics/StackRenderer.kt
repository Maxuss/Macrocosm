package space.maxus.macrocosm.graphics

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.translation.GlobalTranslator
import space.maxus.macrocosm.InternalMacrocosmPlugin
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.FnRet
import space.maxus.macrocosm.util.stripTags
import space.maxus.macrocosm.util.unreachable
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.text.AttributedString
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO


class StackRenderer(
    val component: ItemRenderBuffer,
    val fontProvider: FnRet<Font> = { Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE) }
) {
    companion object {
        private const val FONT_SIZE = 50
        private const val SHADOW_COLOR = 0.2
        private const val TEXT_OFFSET = (FONT_SIZE * 0.2).toInt()

        private const val LINE_OFFSET = (FONT_SIZE * 0.2).toInt()
        private const val BACKGROUND_OFFSET = (FONT_SIZE * 0.1).toInt()
        private const val TEXT_SHADOW_OFFSET = (FONT_SIZE * 0.1).toInt()

        private const val LINE_SIZE = (FONT_SIZE * 0.10).toInt()

        private const val NAME_LORE_DIVIDER_SIZE = FONT_SIZE / 2

        private val FALLBACK_FONT = Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE)
    }

    private lateinit var graphics: Graphics2D
    private lateinit var img: BufferedImage
    private var height: Int = -1
    private var width: Int = -1

    private val __fontLazy by lazy(fontProvider)
    private val font get() = fontProvider()

    fun render(): BufferedImage {
        beginRenderProcess()
        renderBackground()
        renderText()
        graphics.dispose()
        return img
    }

    fun renderToFile(file: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            try {
                val image = render()
                ImageIO.write(image, "png", Accessor.access(file).toFile())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun beginRenderProcess() {
        var maxTextWidth = getComponentWidth(component.name)
        var maxTextHeight =
            getComponentHeight(component.name) + (if (component.lines.isNotEmpty()) NAME_LORE_DIVIDER_SIZE else 0) + TEXT_OFFSET + LINE_OFFSET + BACKGROUND_OFFSET + TEXT_SHADOW_OFFSET

        for (part in component.lines) {
            val number = getComponentWidth(part)
            if (number > maxTextWidth) maxTextWidth = number
            maxTextHeight += getComponentHeight(part)
        }

        width = maxTextWidth.toInt() + TEXT_OFFSET * 2 + 20
        height =
            if (component.lines.isEmpty()) (1.5 * getComponentHeight(component.name)).toInt() else maxTextHeight.toInt()

        img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        graphics = img.createGraphics()

        graphics.color = Color(0, 0, 0)
        graphics.fillRect(0, 0, width, height)
    }

    private fun renderBackground() {
        graphics.color = Color(22, 8, 24)
        graphics.fillRect(
            BACKGROUND_OFFSET,
            BACKGROUND_OFFSET,
            width - BACKGROUND_OFFSET * 2,
            height - BACKGROUND_OFFSET * 2
        )

        graphics.color = Color(46, 6, 95)

        graphics.fillRect(LINE_OFFSET, LINE_OFFSET, LINE_SIZE, height - LINE_OFFSET * 2)
        graphics.fillRect(LINE_OFFSET, LINE_OFFSET, width - LINE_OFFSET * 2, LINE_SIZE)

        graphics.fillRect(LINE_OFFSET, height - LINE_OFFSET - LINE_SIZE, width - LINE_OFFSET - LINE_OFFSET, LINE_SIZE)
        graphics.fillRect(width - LINE_OFFSET - LINE_SIZE, LINE_OFFSET, LINE_SIZE, height - LINE_OFFSET * 2)

        graphics.color = Color(255, 255, 255)
    }

    private fun renderText() {
        val textCurrentX = LINE_OFFSET + BACKGROUND_OFFSET + TEXT_OFFSET
        var textCurrentY = (LINE_OFFSET + BACKGROUND_OFFSET + getComponentHeight(component.name) * 0.70).toInt()

        graphics.font = font
        renderSingleComponent(null, __fontLazy, component.name, textCurrentX, textCurrentY)

        textCurrentY += (getComponentHeight(component.name).toInt() + NAME_LORE_DIVIDER_SIZE)

        for (part in component.lines) {
            renderSingleComponent(null, __fontLazy, part, textCurrentX, textCurrentY)
            textCurrentY += getComponentHeight(part).toInt()
        }

        graphics.color = Color(0, 255, 0)
    }

    private fun renderSingleComponent(currentColor: Color?, currentFont: Font, c: Component, x: Int, y: Int): Int {
        val color = Color(c.color()?.value() ?: currentColor?.rgb ?: NamedTextColor.WHITE.value())
        var font = currentFont
        var text = "null"
        if (c is TextComponent) {
            text = c.content()
        } else if (c is TranslatableComponent) {
            val rendered = (GlobalTranslator.render(c, Locale.ENGLISH))
            if (rendered is TextComponent) {
                text = rendered.content()
            } else if (rendered is TranslatableComponent) {
                text = (rendered.args().first() as TextComponent).children().first().str().stripTags()
            }
        }
        c.decorations().forEach { (it, state) ->
            if (state == null)
            // how tf would state even be null??? its literally marked as `not null`
                unreachable()
            when (state) {
                TextDecoration.State.NOT_SET -> { /* no-op */
                }

                TextDecoration.State.FALSE -> {
                    font = font.deriveFont(Font.PLAIN)
                }

                TextDecoration.State.TRUE -> {
                    if (it == null)
                    // once again, the decoration is literally stated that it may not be
                    // null, wtf??
                        unreachable()
                    when (it) {
                        TextDecoration.OBFUSCATED -> {
                            text = "█".repeat(text.length)
                        }

                        TextDecoration.BOLD -> {
                            font = InternalMacrocosmPlugin.FONT_MINECRAFT_BOLD.deriveFont(50f)
                        }

                        TextDecoration.ITALIC -> {
                            font = InternalMacrocosmPlugin.FONT_MINECRAFT_ITALIC.deriveFont(50f)
                        }

                        else -> {
                            // unsupported
                        }
                    }
                }
            }
        }
        graphics.color = color
        graphics.font = font

        val textStr = createFallbackString(text, font, FALLBACK_FONT)

        graphics.font = font

        // shadow
        val shadowColor = Color(
            (color.red * SHADOW_COLOR).toInt(),
            (color.green * SHADOW_COLOR).toInt(),
            (color.blue * SHADOW_COLOR).toInt()
        )

        graphics.color = shadowColor
        graphics.drawString(textStr.iterator, x + TEXT_SHADOW_OFFSET, y + TEXT_SHADOW_OFFSET)

        // actual contents
        graphics.color = color
        graphics.drawString(textStr.iterator, x, y)

        var xOffset = getTextWidth(font, text).toInt()
        for (child in c.children()) {
            xOffset += renderSingleComponent(color, font, child, x + xOffset, y)
        }
        return xOffset
    }

    private fun createFallbackString(text: String, mainFont: Font, fallbackFont: Font): AttributedString {
        val result = AttributedString(text)
        if (text.isBlank())
            return result
        val textLength = text.length
        result.addAttribute(TextAttribute.FONT, mainFont, 0, textLength)
        var fallback = false
        var fallbackBegin = 0
        for (i in text.indices) {
            val curFallback = !mainFont.canDisplay(text[i])
            if (curFallback != fallback) {
                fallback = curFallback
                if (fallback) {
                    fallbackBegin = i
                } else {
                    result.addAttribute(TextAttribute.FONT, fallbackFont, fallbackBegin, i)
                }
            }
        }
        return result
    }

    private fun getTextWidth(font: Font, text: String): Double {
        val (canDisplay, cantDisplay) = text.partition { font.canDisplay(it) }
        return font.getStringBounds(
            canDisplay,
            FontRenderContext(font.transform, false, false)
        ).bounds.getWidth() + FALLBACK_FONT.getStringBounds(
            cantDisplay, FontRenderContext(
                FALLBACK_FONT.transform, false, false
            )
        ).bounds.getWidth()
    }

    private fun getTextHeight(font: Font, text: String): Double {
        return font.getStringBounds(text, FontRenderContext(font.transform, false, false)).bounds.getHeight()
    }

    private fun getComponentWidth(single: Component): Double {
        return getTextWidth(__fontLazy, single.str().stripTags())
    }

    private fun getComponentHeight(single: Component): Double {
        return getTextHeight(__fontLazy, single.str().stripTags())
    }
}
