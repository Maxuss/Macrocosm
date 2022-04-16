package space.maxus.macrocosm.ability

import space.maxus.macrocosm.ability.types.InstantTransmission

enum class Ability(private val ability: ItemAbility) {
    INSTANT_TRANSMISSION(InstantTransmission)
    ;

    companion object {
        fun init() {
            for (abil in values()) {
                AbilityRegistry.register(abil.name, abil.ability)
            }
        }
    }
}
