package space.maxus.macrocosm.ability.equipment

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.EquipmentAbility
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic

object RingOfLifeAbility: EquipmentAbility("Life Boost", "Increases your ${Statistic.HEALTH.display}<gray> by <green>5%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.BELT, ItemType.NECKLACE))
                return@listen

            e.stats.health *= 1.05f
        }
    }
}

object NecklaceOfEnduranceAbility: EquipmentAbility("Endurance Boost", "Increases your ${Statistic.VITALITY.display}<gray> by <green>15%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE))
                return@listen

            e.stats.vitality *= 1.15f
        }
    }
}

object VialOfLifebloodAbility: EquipmentAbility("Living Protection", "Boosts your ${Statistic.TRUE_DEFENSE.display} by <green>20%<gray> when your ${Statistic.HEALTH.display}<gray> is over <red>50%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.BELT, ItemType.NECKLACE))
                return@listen

            if(e.player.currentHealth / e.stats.health >= .5f)
                e.stats.trueDefense *= 1.2f
        }
    }
}

object ElixirOfLifeAbility: EquipmentAbility("Mystical Elixir", "Boosts your ${Statistic.VITALITY.display}<gray> and ${Statistic.TRUE_DEFENSE.display}<gray> by <green>25%<gray> and ${Statistic.HEALTH.display}<gray> by <green>10%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.BELT, ItemType.NECKLACE))
                return@listen

            e.stats.trueDefense *= 1.25f
            e.stats.vitality *= 1.25f
            e.stats.health *= 1.10f
        }
    }
}
