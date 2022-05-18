package space.maxus.macrocosm.item.buffs

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.Identifier

interface MinorItemBuff {
    val id: Identifier

    fun stats(item: MacrocosmItem, tier: Int): Statistics
    fun buildFancy(amount: Int): Component
}