package space.maxus.macrocosm.slayer

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.display.RenderComponent
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.stripTags

class SlayerDisplay(val type: SlayerType, val tier: Int, val collectedExp: Float, val status: SlayerStatus) :
    RenderComponent {
    override fun title(): Component {
        return Component.text("${type.slayer.name.stripTags()} ${roman(tier)}").color(colorFromTier(tier))
    }

    override fun lines(): List<Component> {
        return listOf(text(""))
    }
}
