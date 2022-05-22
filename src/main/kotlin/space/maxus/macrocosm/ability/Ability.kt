package space.maxus.macrocosm.ability

import space.maxus.macrocosm.ability.types.AmethystArmorBonus
import space.maxus.macrocosm.ability.types.EmeraldArmorBonus
import space.maxus.macrocosm.ability.types.EmeraldPickaxeBonus
import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.item.types.WITHER_SCROLL_IMPLOSION
import space.maxus.macrocosm.item.types.WITHER_SCROLL_SHADOW_WARP
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_IMPACT
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_SHIELD
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.id

enum class Ability(val ability: ItemAbility) {
    INSTANT_TRANSMISSION(InstantTransmission),
    IMPLOSION(WITHER_SCROLL_IMPLOSION),
    WITHER_SHIELD(WITHER_SCROLL_WITHER_SHIELD),
    SHADOW_WARP(WITHER_SCROLL_SHADOW_WARP),
    WITHER_IMPACT(WITHER_SCROLL_WITHER_IMPACT),

    EMERALD_AFFECTION(EmeraldArmorBonus),
    EMERALD_AFFECTION_PICKAXE(EmeraldPickaxeBonus),

    DEFLECTION(AmethystArmorBonus)

    ;

    companion object {
        fun init() {
            Registry.ABILITY.delegateRegistration(values().map { id(it.name.lowercase()) to it.ability }) { _: Identifier, _: ItemAbility -> }
        }
    }
}
