package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.text.comp

abstract class Slayer(
    val name: String,
    val item: Material,
    val id: String,
    val description: String,
    val validEntities: List<EntityType>,
    val requiredExp: List<Double>,
    val tiers: IntRange
): Listener {
    abstract fun abilitiesForTier(tier: Int): List<SlayerAbility>
    abstract fun bossForTier(tier: Int): MacrocosmEntity
    abstract fun minisForTier(tier: Int): List<MacrocosmEntity>

    fun descript(): List<Component> {
        val reduced = description.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }
}
