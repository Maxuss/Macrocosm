package space.maxus.macrocosm.ability

import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.util.Identifier

enum class Ability(private val ability: ItemAbility) {
    INSTANT_TRANSMISSION(InstantTransmission)
    ;

    companion object {
        fun init() {
            for (abil in values()) {
                AbilityRegistry.register(Identifier.macro(abil.name.lowercase()), abil.ability)
            }
        }
    }
}
