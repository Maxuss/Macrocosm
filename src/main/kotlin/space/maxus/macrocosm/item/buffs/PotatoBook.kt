package space.maxus.macrocosm.item.buffs

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.id

object PotatoBook : MinorItemBuff {
    override val id: Identifier = id("potato_book")

    override fun stats(item: MacrocosmItem, tier: Int): Statistics {
        return space.maxus.macrocosm.stats.stats {
            if (item.type.armor) {
                health = tier * 4f
                defense = tier * 4f
            } else {
                damage = tier * 2f
                strength = tier * 2f
            }
        }
    }

    override fun buildFancy(amount: Int): Component {
        return text("<yellow>(+$amount)</yellow>")
    }
}
