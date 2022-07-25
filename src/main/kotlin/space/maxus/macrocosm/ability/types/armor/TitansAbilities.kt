package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.min

object TitansKnowledge: AbilityBase(AbilityType.PASSIVE, "Titan's Knowledge", "This mysterious helmet drains your ability power<gray>.<br>Your ${Statistic.ABILITY_DAMAGE.display}<gray> is <green>halved<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HEAD))
                return@listen

            e.stats.abilityDamage /= 2f
        }
    }
}

object TitansEnergy: AbilityBase(AbilityType.PASSIVE, "Titan's Energy", "This magical cuirass drains your mana pool.<br>Your ${Statistic.INTELLIGENCE.display}<gray> is capped at <green>10x<gray> of your ${Statistic.ABILITY_DAMAGE.display}<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.CHEST))
                return@listen

            val cap = e.stats.abilityDamage * 10f
            e.stats.intelligence = min(cap, e.stats.intelligence)
        }
    }
}

object TitansMight: AbilityBase(AbilityType.PASSIVE, "Titan's Might", "This powerful gladius is light, making it hard to parry with.<br>Your ${Statistic.DEFENSE.display}<gray> is capped at <green>2x<gray> the <green>sum of<gray> your ${Statistic.SPEED.display}<gray> and ${Statistic.BONUS_ATTACK_SPEED.display}<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val cap = 2f * (e.stats.speed + e.stats.attackSpeed)
            e.stats.defense = min(cap, e.stats.defense)
        }
    }
}

object TitansValor: AbilityBase(AbilityType.PASSIVE, "Titan's Valor", "This gleaming shield is unimaginably heavy.<br>Your ${Statistic.STRENGTH.display}<gray> is capped at <green>1/2<gray> of your ${Statistic.DEFENSE.display}<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen

            val cap = e.stats.defense / 2f
            e.stats.strength = min(cap, e.stats.strength)
        }
    }
}
