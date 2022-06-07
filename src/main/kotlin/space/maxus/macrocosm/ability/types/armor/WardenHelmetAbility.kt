package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.roundToInt

object WardenHelmetAbility: AbilityBase(AbilityType.PASSIVE, "Brute Force", "Lose all of your speed over <white>200<gray> but gain <red>+10% ${Statistic.STRENGTH.display}<gray> for each <green>50 Speed<gray> lost.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent>(priority = EventPriority.LOWEST) { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HEAD))
                return@listen
            val speed = e.stats.speed
            if(speed < 200)
                return@listen
            val times = (speed - 200).roundToInt() / 50
            e.stats.strength *= 1 + (.1f * times)
            e.stats.speed = 200f
        }
    }
}
