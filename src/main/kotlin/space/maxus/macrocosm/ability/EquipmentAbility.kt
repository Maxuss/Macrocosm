package space.maxus.macrocosm.ability

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.MacrocosmPlayer

abstract class EquipmentAbility(name: String, description: String): AbilityBase(AbilityType.PASSIVE, name, description) {
    private fun ensureRequirements(player: MacrocosmPlayer, slot: ItemType): Boolean {
        return player.equipment[slot]?.abilities?.contains(this) == true
    }
}
