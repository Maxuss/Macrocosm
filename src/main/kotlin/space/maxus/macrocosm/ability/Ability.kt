package space.maxus.macrocosm.ability

import org.bukkit.entity.EntityType
import space.maxus.macrocosm.ability.types.armor.*
import space.maxus.macrocosm.ability.types.item.*
import space.maxus.macrocosm.ability.types.other.EntityDamageMulAbility
import space.maxus.macrocosm.ability.types.other.ShortbowAbility
import space.maxus.macrocosm.ability.types.other.SlayerQuestAbility
import space.maxus.macrocosm.item.types.WITHER_SCROLL_IMPLOSION
import space.maxus.macrocosm.item.types.WITHER_SCROLL_SHADOW_WARP
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_IMPACT
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_SHIELD
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.Statistic
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
    INFINITE_TERROR(InfiniteTerrorAbility),
    DEATH_DEFY(DeathDefyAbility),
    ANTIMATTER_RAY(VoidPrismAbility),
    PURE_HATRED(RancorousStaffAbility),

    // slayers

    // zombie
    NECROMANTIC_RITUAL(SlayerQuestAbility("Necromantic Ritual", "Make a <gold>Blood Sacrifice<gray> and start a ritual to summon the <red>Profaned Revenant Boss<gray>. <br><red>The magic glyphs on this scroll<br><red>will disappear on use!<gray>.", SlayerType.REVENANT_HORROR, 6)),
    BRUTE_FORCE(WardenHelmetAbility),
    REAPER_BLOOD(ReaperMaskAbility),
    FOREVER_IN_TOMB(EntombedMaskAbility),
    MASTER_NECROMANCER(MasterNecromancerBonus),

    UNDEAD_SWORD(EntityDamageMulAbility("Undead Damage", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), 0.5f)),
    REVENANT_FALCHION(EntityDamageMulAbility("Revenant Damage", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), 1f)),
    REAPER_FALCHION(EntityDamageMulAbility("Reaper Damage", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), 2f)),
    REAPER_WEAPON(EntityDamageMulAbility("Sycophant", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), 2.5f)),
    REAPER_SCYTHE(ReaperScytheAbility),

    REVENANT_LIFE_STEAL(LifeStealAbility(10)),
    REAPER_LIFE_STEAL(LifeStealAbility(25)),

    AOTS_THROW(AOTSAbility),

    REVENANT_ARMOR_BONUS(EntityKillCounterBonus("Undead Bulwark", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), Statistic.DEFENSE)),
    REAPER_ARMOR_BONUS(EntityKillCounterBonus("Reaper Bulwark", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), Statistic.DEFENSE, listOf(
        0,
        30,
        80,
        120,
        150,
        200,
        250,
        300,
        350,
        400
    ))),

    SMALL_HEAL(ZombieWandAbility("Small Heal", 50f, 4, 100)),
    MEDIUM_HEAL(ZombieWandAbility("Medium Heal", 90f, 5, 150)),
    BIG_HEAL(ZombieWandAbility("Big Heal", 120f, 6, 150)),
    HUGE_HEAL(ZombieWandAbility("Huge Heal", 150f, 7, 250)),

    NEGATE(ZombieHeartHealing("Negate", 10f)),
    VITIATE(ZombieHeartHealing("Vitiate", 25f)),
    BELIE(ZombieHeartHealing("Belie", 50f))

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
