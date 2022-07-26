package space.maxus.macrocosm.ability

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.MacrocosmPlayer

abstract class EquipmentAbility(name: String, description: String): AbilityBase(AbilityType.PASSIVE, name, description) {
    protected fun ensureRequirements(player: MacrocosmPlayer, vararg slots: ItemType): Boolean {
        return slots.map { slot -> player.equipment[slot]?.abilities?.contains(this) == true }.any { it }
    }
}
