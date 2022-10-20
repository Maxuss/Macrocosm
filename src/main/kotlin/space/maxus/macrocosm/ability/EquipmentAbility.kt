package space.maxus.macrocosm.ability

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.MacrocosmPlayer

/**
 * An abstract class describing an ability bound to equipment items
 *
 * @constructor
 * @see space.maxus.macrocosm.ability.AbilityBase
 *
 * @param name Ability name
 * @param description Ability description, supports minimessage formatting
 */
abstract class EquipmentAbility(name: String, description: String) :
    AbilityBase(AbilityType.PASSIVE, name, description) {
    protected fun ensureRequirements(player: MacrocosmPlayer, vararg slots: ItemType): Boolean {
        return slots.map { slot -> player.equipment[slot]?.abilities?.contains(this) == true }.any { it }
    }
}
