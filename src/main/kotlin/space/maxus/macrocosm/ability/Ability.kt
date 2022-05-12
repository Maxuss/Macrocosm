package space.maxus.macrocosm.ability

import space.maxus.macrocosm.ability.types.EmeraldArmorBonus
import space.maxus.macrocosm.ability.types.EmeraldPickaxeBonus
import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.item.types.WITHER_SCROLL_IMPLOSION
import space.maxus.macrocosm.item.types.WITHER_SCROLL_SHADOW_WARP
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_IMPACT
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_SHIELD
import space.maxus.macrocosm.util.Identifier

enum class Ability(val ability: ItemAbility) {
    INSTANT_TRANSMISSION(InstantTransmission),
    IMPLOSION(WITHER_SCROLL_IMPLOSION),
    WITHER_SHIELD(WITHER_SCROLL_WITHER_SHIELD),
    SHADOW_WARP(WITHER_SCROLL_SHADOW_WARP),
    WITHER_IMPACT(WITHER_SCROLL_WITHER_IMPACT),

    EMERALD_AFFECTION(EmeraldArmorBonus),
    EMERALD_AFFECTION_PICKAXE(EmeraldPickaxeBonus)

    ;

    companion object {
        fun init() {
            for (abil in values()) {
                AbilityRegistry.register(Identifier.macro(abil.name.lowercase()), abil.ability)
            }
        }
    }
}
