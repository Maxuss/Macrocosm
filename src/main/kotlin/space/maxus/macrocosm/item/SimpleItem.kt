package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.stats.Statistics

data class SimpleItem(private val itemName: String, override val base: Material, override val rarity: Rarity) : MacrocosmItem {
    override var stats: Statistics = Statistics.zero()

    override val name: Component = itemName.toComponent()
    override var rarityUpgraded: Boolean = false
}
