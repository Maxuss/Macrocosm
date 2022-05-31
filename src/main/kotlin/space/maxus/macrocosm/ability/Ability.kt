package space.maxus.macrocosm.ability

import space.maxus.macrocosm.ability.types.armor.*
import space.maxus.macrocosm.ability.types.item.*
import space.maxus.macrocosm.ability.types.other.ShortbowAbility
import space.maxus.macrocosm.item.types.WITHER_SCROLL_IMPLOSION
import space.maxus.macrocosm.item.types.WITHER_SCROLL_SHADOW_WARP
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_IMPACT
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_SHIELD
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.id

/**
 * Enum used to easily access most simple ability types
 *
 * @property ability The ability itself stored inside this enum
 */
enum class Ability(val ability: MacrocosmAbility) {
    INSTANT_TRANSMISSION(InstantTransmission),
    IMPLOSION(WITHER_SCROLL_IMPLOSION),
    WITHER_SHIELD(WITHER_SCROLL_WITHER_SHIELD),
    SHADOW_WARP(WITHER_SCROLL_SHADOW_WARP),
    WITHER_IMPACT(WITHER_SCROLL_WITHER_IMPACT),

    EMERALD_AFFECTION(EmeraldArmorBonus),
    EMERALD_AFFECTION_PICKAXE(EmeraldPickaxeBonus),

    DEFLECTION(AmethystArmorBonus),
    HONEY_BARRAGE(BeekeeperArmorBonus),

    SHORTBOW_GENERIC(ShortbowAbility),

    HONEYCOMB_BULWARK(HoneycombBulwarkAbility),

    OLD_DRAGON(OldDragonBonus),
    PROTECTOR_DRAGON(ProtectorDragonBonus),
    STRONG_DRAGON(StrongDragonBonus),
    SUPERIOR_DRAGON(SuperiorDragonBonus),
    UNSTABLE_DRAGON(UnstableDragonBonus),
    WISE_DRAGON(WiseDragonBonus),
    YOUNG_DRAGON(YoungDragonBonus),
    AOTD(AOTDAbility),
    ICE_CONE(IceConeAbility),
    TERRAIN_TOSS(TerrainTossAbility),
    INFINITE_TERROR(InfiniteTerrorAbility)

    ;

    companion object {
        /**
         * Initializes this ability, storing all data inside the [Registry.ABILITY]
         *
         */
        fun init() {
            Registry.ABILITY.delegateRegistration(values().map { id(it.name.lowercase()) to it.ability })
        }
    }
}
