package space.maxus.macrocosm.ability.types.other

import net.axay.kspigot.event.listen
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.stats.Statistic

private fun EntityType.pretty(): String {
    return when (this) {
        EntityType.ENDERMAN -> "Endermen"
        EntityType.SILVERFISH -> "Silverfish"
        EntityType.DROWNED -> "Drowned"
        else -> "${name.replace("_", " ").capitalized(" <blue>")}s"
    }
}

private fun descriptAbility(family: MutableList<EntityType>, mul: Float): String {
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
    return "Deal <red>+${Formatting.stats((mul * 100f).toBigDecimal())}% ${Statistic.DAMAGE.display}<gray> towards $str<gray>."
}


class EntityDamageMulAbility(name: String, private val affected: List<EntityType>, private val multiplier: Float): AbilityBase(
    AbilityType.PASSIVE, name, descriptAbility(affected.toMutableList(), multiplier)) {

    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            if(!affected.contains(e.damaged.type))
                return@listen
            e.damage *= (1 + multiplier)
        }
    }
}
