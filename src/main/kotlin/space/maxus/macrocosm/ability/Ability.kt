package space.maxus.macrocosm.ability

import org.bukkit.entity.EntityType
import space.maxus.macrocosm.ability.types.armor.*
import space.maxus.macrocosm.ability.types.equipment.*
import space.maxus.macrocosm.ability.types.item.*
import space.maxus.macrocosm.ability.types.other.EntityDamageMulAbility
import space.maxus.macrocosm.ability.types.other.ShortbowAbility
import space.maxus.macrocosm.ability.types.other.SlayerQuestAbility
import space.maxus.macrocosm.ability.types.turret.*
import space.maxus.macrocosm.item.types.WITHER_SCROLL_IMPLOSION
import space.maxus.macrocosm.item.types.WITHER_SCROLL_SHADOW_WARP
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_IMPACT
import space.maxus.macrocosm.item.types.WITHER_SCROLL_WITHER_SHIELD
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.general.id

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
    NECROMANTIC_RITUAL(
        SlayerQuestAbility(
            "Necromantic Ritual",
            "Make a <gold>Blood Sacrifice<gray> and start a ritual to summon the <red>Profaned Revenant Boss<gray>. <br><red>The magic glyphs on this scroll<br><red>will disappear on use!<gray>.",
            SlayerType.REVENANT_HORROR,
            6
        )
    ),
    BRUTE_FORCE(WardenHelmetAbility),
    REAPER_BLOOD(ReaperMaskAbility),
    FOREVER_IN_TOMB(EntombedMaskAbility),
    MASTER_NECROMANCER(MasterNecromancerBonus),

    UNDEAD_SWORD(
        EntityDamageMulAbility(
            "Undead Damage",
            listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK),
            0.5f
        )
    ),
    REVENANT_FALCHION(
        EntityDamageMulAbility(
            "Revenant Damage",
            listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK),
            1f
        )
    ),
    REAPER_FALCHION(
        EntityDamageMulAbility(
            "Reaper Damage",
            listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK),
            2f
        )
    ),
    REAPER_WEAPON(
        EntityDamageMulAbility(
            "Sycophant",
            listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK),
            2.5f
        )
    ),
    REAPER_SCYTHE(ReaperScytheAbility),

    REVENANT_LIFE_STEAL(LifeStealAbility(10)),
    REAPER_LIFE_STEAL(LifeStealAbility(25)),

    AOTS_THROW(AOTSAbility),

    REVENANT_ARMOR_BONUS(
        EntityKillCounterBonus(
            "Undead Bulwark",
            listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK),
            Statistic.DEFENSE
        )
    ),
    REAPER_ARMOR_BONUS(
        EntityKillCounterBonus(
            "Reaper Bulwark", listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK), Statistic.DEFENSE, listOf(
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
            )
        )
    ),

    GHOUL_BUSTER_ARMOR_BONUS(GhoulBusterAbility),

    ROTTEN_HEART_T1(RottenHeartAbility(.75f)),
    ROTTEN_HEART_T2(RottenHeartAbility(1.5f)),

    HEAT_RESISTANT_ARMOR_BONUS(
        EntityKillCounterBonus(
            "Heat Resistance",
            listOf(EntityType.WITHER_SKELETON),
            Statistic.DEFENSE
        )
    ),
    VOLCANO_STRIDER_ARMOR_BONUS(
        EntityKillCounterBonus(
            "Volcano Strider", listOf(EntityType.WITHER_SKELETON), Statistic.DEFENSE, listOf(
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
            )
        )
    ),

    ENRAGE_ARMOR_ABILITY(EnrageAbility),

    FIRE_BRANDS_ARMOR_BONUS(FireBrandAbility("Fire Brands", 1000f)),
    FIRE_VORTICES_ARMOR_BONUS(FireBrandAbility("Fire Vortices", 2500f)),

    SMALL_HEAL(ZombieWandAbility("Small Heal", 50f, 4, 100)),
    MEDIUM_HEAL(ZombieWandAbility("Medium Heal", 90f, 5, 150)),
    BIG_HEAL(ZombieWandAbility("Big Heal", 120f, 6, 150)),
    HUGE_HEAL(ZombieWandAbility("Huge Heal", 150f, 7, 250)),

    NEGATE(ZombieHeartHealing("Negate", 10f)),
    VITIATE(ZombieHeartHealing("Vitiate", 25f)),
    BELIE(ZombieHeartHealing("Belie", 50f)),
    ACUPUNCTURE(VoodooDollAbility),

    // wither
    CATACLYSM(EntityDamageMulAbility("Fading Hatred", listOf(EntityType.WITHER_SKELETON), 0.5f)),
    CATASTROPHE(EntityDamageMulAbility("Decaying Animus", listOf(EntityType.WITHER_SKELETON), 1f)),
    CALAMITY(EntityDamageMulAbility("Withering Malice", listOf(EntityType.WITHER_SKELETON), 2f)),

    DECADENCE(DecadenceAbility),
    LETHARGY(LethargyAbility),
    MYSPYS(MyspysAbility),

    FLAMETHROWER(FlamethrowerAbility),

    HUNTERS_STILETTO_PASSIVE(HuntersStilettoPassive),
    HUNTERS_STILETTO_ACTIVE(HuntersStilettoActive),
    ASSASSINS_DAGGER_ABILITY(AssassinsDaggerAbility),
    MOLEPICK_ABILITY(MolepickAbility),
    EARTHQUAKE_MALLET_ACTIVE(EarthquakeMalletAbility),
    EARTHQUAKE_MALLET_PASSIVE(BeatingBagAbility),

    TESLA_COIL_ACTIVE(TeslaCoilActive),
    TESLA_COIL_PASSIVE(TeslaCoilPassive),

    FLAMETHROWER_TURRET_ACTIVE(FlamethrowerTurretActive),
    FLAMETHROWER_TURRET_PASSIVE(FlamethrowerTurretPassive),

    HEAVY_TURRET_ACTIVE(HeavyTurretActive),
    HEAVY_TURRET_PASSIVE(HeavyTurretPassive),

    FIERY_SLASH(FierySlashAbility),
    INFERNAL_GREATSWORD_THROW(InfernalGreatswordThrowAbility),
    BERSERKER(BerserkerAbility),
    IV_BAG(IVBagAbility),

    // reliquary
    TITANS_KNOWLEDGE(TitansKnowledge),
    TITANS_ENERGY(TitansEnergy),
    TITANS_MIGHT(TitansMight),
    TITANS_VALOR(TitansValor),

    TITANS_EMINENCE(TitansEminence),
    TITANS_LIGHTNING(TitansLightning),

    BRITTLE_BONES(BrittleBonesAbility),
    BLACKSHARD_ABILITY(BlackshardAbility),
    SKULL_HELMET_ABILITY(SkullHelmetAbility),
    SOUL_STRIKE_ABILITY(ShieldOfDeadAbility),

    ARMOR_OF_DAMNED_ACTIVE(ArmorOfDamnedActive),
    ARMOR_OF_DAMNED_PASSIVE(ArmorOfDamnedPassive),

    // equipment
    RING_OF_LIFE(RingOfLifeAbility),
    NECKLACE_OF_ENDURANCE(NecklaceOfEnduranceAbility),
    VIAL_OF_LIFEBLOOD(VialOfLifebloodAbility),
    ELIXIR_OF_LIFE(ElixirOfLifeAbility),

    AMULET_OF_THE_UNDERTAKER_ABILITY(AmuletOfTheUndertakerAbility),
    DEAD_MANS_BOOTS_ABILITY(DeadMansBootsAbility),
    VAMPIRES_COWL_ABILITY(VampiresCowlAbility),
    CLOAK_OF_UNDEAD_KING_1(CloakOfUndeadKing1),
    CLOAK_OF_UNDEAD_KING_2(CloakOfUndeadKing2),

    CHARM_OF_MANA_ABILITY(CharmOfManaAbility),
    TALISMAN_OF_MANA_ABILITY(TalismanOfManaAbility),
    MANA_ORB_ABILITY(ManaOrbAbility),
    WIZARDS_WELL_1(WizardsWellAbility1),
    WIZARDS_WELL_2(WizardsWellAbility2),

    // elements
    BLESSED_POLARITIES(BlessedPolaritiesAbility),
    CURSED_POLARITIES(CursedPolaritiesAbility),
    FORBIDDEN_POLARITIES(ForbiddenPolaritiesAbility),
    POLARITY_ORIGINS(PolarityOriginsAbility),
    ENVOY_OF_POLARITIES(EnvoyOfPolarities),

    ORDER_POLARITY(OrderSetAbility),
    EARTH_POLARITY(EarthPolaritySetBonus),
    FIRE_POLARITY(FirePolaritySetBonus),

    WATER_POLARITY(WaterPolaritySetBonus),
    AIR_POLARITY_1(AirPolaritySetBonus1),
    AIR_POLARITY_2(AirPolaritySetBonus2),
    BLOOD_POLARITY(BloodPolaritySetBonus),

    SPIRITS_POLARITY(SpiritsPolaritySetBonus),
    VOID_POLARITY(VoidPolaritySetBonus),
    CHAOS_POLARITY_1(ChaosPolaritySetBonus1),
    CHAOS_POLARITY_2(ChaosPolaritySetBonus2),

    CONFLUX_POLARITY(ConfluxPolarity),

    CAT_EARS(CatEarsAbility)
    ;

    val ptr by lazy { RegistryPointer(Identifier.macro("ability"), Identifier.macro(name.lowercase())) }

    companion object {
        /**
         * Initializes all abities, storing all data inside the ability registry [Registry.ABILITY]
         */
        fun init() {
            Registry.ABILITY.delegateRegistration(values().map { id(it.name.lowercase()) to it.ability })
        }
    }
}
