package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.*
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

private fun EntityType.pretty(): String {
    return when (this) {
        EntityType.ENDERMAN -> "Endermen"
        EntityType.SILVERFISH -> "Silverfish"
        EntityType.DROWNED -> "Drowned"
        else -> "${name.replace("_", " ").capitalized(" <blue>")}s"
    }
}

private fun familyToDescription(family: MutableList<EntityType>): String {
    val str = StringBuilder()
    if (family.size > 1) {
        val last = family.removeAt(family.size - 1)
        for (mob in family) {
            str.append("<blue>${mob.pretty()}")
            if (family.last() != mob)
                str.append("<gray>, ")
        }
        str.append("<gray> and <blue>${last.pretty()}<gray>")
    } else
        str.append("<blue>${family.last().pretty()}<gray>")
    return "Increases ${Statistic.DAMAGE.display}<gray> you deal towards $str by <red>{{MUL}}%<gray>."
}

class MobEnchantment(
    name: String,
    private val affectedMobs: List<EntityType>,
    conflicts: List<String>,
    levels: IntRange = 1..7,
    applicable: List<ItemType> = ItemType.melee(),
    private val multiplier: Float = .15f
) : EnchantmentBase(name, "NULL", levels, applicable, conflicts = conflicts) {
    override fun description(level: Int): List<Component> {
        val str = familyToDescription(affectedMobs.toMutableList()).replace(
            "{{MUL}}",
            Formatting.stats((level * multiplier * 100).toBigDecimal(), true)
        )
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        val (ok, level) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok || !affectedMobs.contains(e.damaged.type))
            return
        val modifier = 1 + (level * multiplier)
        e.damage *= modifier
    }
}
