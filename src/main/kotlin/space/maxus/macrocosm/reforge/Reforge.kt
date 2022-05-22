package space.maxus.macrocosm.reforge

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Clone
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

@Suppress("unused")
interface Reforge : Listener, Clone {
    val abilityName: String?
    val abilityDescription: String?
    val name: String
    val applicable: List<ItemType>

    fun stats(rarity: Rarity): Statistics

    fun buildLore(lore: MutableList<Component>) {
        if (abilityName == null || abilityDescription == null)
            return

        lore.add(comp("<blue>$abilityName <blue>($name Bonus)").noitalic())
        val truncated = abilityDescription!!.reduceToList()
        lore.addAll(truncated.map { comp("<gray>$it").noitalic() })
        lore.add("".toComponent())
    }

    override fun clone(): Reforge {
        throw IllegalStateException("Override the clone method inside Reforge")
    }
}
