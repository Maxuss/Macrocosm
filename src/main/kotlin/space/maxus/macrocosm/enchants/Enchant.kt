package space.maxus.macrocosm.enchants

import org.bukkit.entity.EntityType
import space.maxus.macrocosm.enchants.type.*
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.specialStats
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.Identifier

enum class Enchant(private val enchant: Enchantment) {
    SHARPNESS(
        SimpleEnchantment(
            "Sharpness",
            "Increases all damage you deal towards your enemies by <damage_boost>%<gray>.",
            1..7,
            ItemType.melee(),
            stats {
                damageBoost = 0.1f
            },
            conflicts = listOf("BANE_OF_ARTHROPODS", "SMITE", "COLD_TOUCH", "CUBISM", "ENDER_SLAYER"),
        )
    ),

    // mob enchantments
    SMITE(
        MobEnchantment(
            "Smite",
            listOf(
                EntityType.ZOMBIE,
                EntityType.DROWNED,
                EntityType.HUSK,
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.ZOGLIN
            ),
            listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "COLD_TOUCH", "CUBISM", "ENDER_SLAYER")
        )
    ),
    BANE_OF_ARTHROPODS(
        MobEnchantment(
            "Bane of Arthropods",
            listOf(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE),
            listOf("SHARPNESS", "SMITE", "COLD_TOUCH", "CUBISM", "ENDER_SLAYER")
        )
    ),
    COLD_TOUCH(
        MobEnchantment(
            "Cold Touch",
            listOf(EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE),
            listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "SMITE", "CUBISM", "ENDER_SLAYER")
        )
    ),
    CUBISM(
        MobEnchantment(
            "Cubism",
            listOf(EntityType.SLIME, EntityType.MAGMA_CUBE, EntityType.CREEPER, EntityType.STRIDER),
            listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "COLD_TOUCH", "SMITE", "ENDER_SLAYER")
        )
    ),
    ENDER_SLAYER(
        MobEnchantment(
            "Ender Slayer",
            listOf(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.SHULKER),
            listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "COLD_TOUCH", "CUBISM", "SMITE"),
            multiplier = .2f
        )
    ),
    DRAGON_HUNTER(
        MobEnchantment(
            "Dragon Hunter",
            listOf(EntityType.ENDER_DRAGON),
            listOf(),
            1..6,
            multiplier = .1f,
            applicable = ItemType.weapons()
        )
    ),
    IMPALING(
        MobEnchantment(
            "Impaling",
            listOf(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.SQUID, EntityType.GLOW_SQUID),
            listOf(),
            levels = 1..5,
            multiplier = .15f,
            applicable = ItemType.weapons()
        )
    ),


    // stat enchants
    VICIOUS(
        SimpleEnchantment(
            "Vicious",
            "Increases your ${Statistic.FEROCITY.display}<gray> by <ferocity><gray>, which allows you to sometimes strike enemies <blue>twice<gray>.",
            1..4,
            multiplier = 1f,
            applicable = ItemType.weapons(),
            base = stats {
                ferocity = 4f
            },
            conflicts = listOf("RUTHLESS")
        )
    ),
    RUTHLESS(
        SimpleEnchantment(
            "Ruthless",
            "Increases your ${Statistic.BONUS_ATTACK_SPEED.display}<gray> by <bonus_attack_speed><gray>, which allows you to attack your enemies <blue>more often<gray>.",
            1..4,
            multiplier = 1f,
            applicable = ItemType.weapons(),
            base = stats {
                attackSpeed = 5f
            },
            conflicts = listOf("VICIOUS")
        )
    ),
    CRITICAL(
        SimpleEnchantment(
            "Critical",
            "Increases your ${Statistic.CRIT_DAMAGE.display}<gray> by <crit_damage><gray>, which allows you to do more critical hit damage.",
            1..7,
            multiplier = 1f,
            applicable = ItemType.melee(),
            base = stats {
                critDamage = 10f
            },
            conflicts = listOf("PRECISE")
        )
    ),
    PRECISE(
        SimpleEnchantment(
            "Precise",
            "Increases your ${Statistic.CRIT_CHANCE.display}<gray> by <crit_chance><gray>, which allows you to deal <blue>critical hits<gray> more often.",
            1..7,
            multiplier = 0.5f,
            applicable = ItemType.melee(),
            base = stats {
                critChance = 5f
            },
            conflicts = listOf("CRITICAL")
        )
    ),
    KNOCKBACK(
        SimpleEnchantment(
            "Knockback",
            "Increases your <green>Bonus Knockback<gray> by <green><kb_boost>%<gray>, which allows you to strike enemies further from you.",
            1..3,
            multiplier = 1f,
            applicable = ItemType.weapons(),
            special = specialStats {
                knockbackBoost = 0.1f
            }
        )
    ),
    LUCK(
        SimpleEnchantment(
            "Luck",
            "Increases your ${Statistic.MAGIC_FIND.display}<gray> by <green><magic_find><gray>, which grants higher chance of getting rare drops.",
            1..4,
            multiplier = 2f,
            applicable = ItemType.weapons(),
            base = stats {
                magicFind = 3f
            }
        )
    ),

    // <stat>-steal enchants
    LIFE_STEAL(
        StealingEnchantment(
            "Life Steal",
            Statistic.HEALTH,
            0.5f,
            regain = { player, (amount, _) ->
                player.currentHealth = kotlin.math.min(player.currentHealth + amount, player.stats()!!.health)
            },
            conflicts = listOf("SYPHON"),
            levels = 1..5
        )
    ),
    MANA_STEAL(
        StealingEnchantment(
            "Mana Steal",
            Statistic.INTELLIGENCE,
            0.25f,
            regain = { player, (amount, _) ->
                player.currentMana =
                    kotlin.math.min(player.currentMana + amount, player.stats()!!.intelligence)
            },
            levels = 1..3
        )
    ),
    SYPHON(
        StealingEnchantment(
            "Syphon",
            Statistic.HEALTH,
            0.2f,
            actualDescription = "<gray>Heal for <green>{{amount}}%<gray> of your max ${Statistic.HEALTH.display}<gray> per <blue>100 ${Statistic.CRIT_DAMAGE.display}<gray> you deal per hit, up to <blue>1,000 ${Statistic.CRIT_DAMAGE.display}<gray>.",
            regain = { player, (amount, crit) ->
                if (crit) {
                    val stats = player.stats()!!
                    player.currentHealth = kotlin.math.min(
                        player.currentHealth + (amount * (stats.critDamage.toInt() / 100)),
                        stats.health
                    )
                }
            },
            levels = 1..5,
            conflicts = listOf("LIFE_STEAL")
        )
    ),

    THUNDERLORD(ThunderlordEnchantment),
    THUNDERBOLT(ThunderboltEnchantment),

    FIRE_ASPECT(FireAspectEnchantment),
    FROST_ASPECT(FrostAspectEnchantment),

    EXPERIENCE(ExperienceEnchantment),

    GIANT_KILLER(GiantKillerEnchantment),
    TITAN_KILLER(TitanKillerEnchantment),

    PROSECUTE(ProsecuteEnchantment),
    EXECUTE(ExecuteEnchantment),

    LETHALITY(LethalityEnchantment),
    EXHALATION(ExhalationEnchantment),

    FIRST_STRIKE(FirstStrikeEnchantment),
    TRIPLE_STRIKE(TripleStrikeEnchantment),

    CLEAVE(CleaveEnchantment),
    SCAVENGER(ScavengerEnchantment),

    VAMPIRISM(VampirismEnchantment),
    MANA_EXHAUSTION(ManaExhaustionEnchantment),

    // ultimate
    SUPERIOR(UltimateEnchantment(
        "Superior",
        "Increases <blue>ALL<gray> your stats by <red><stat_boost_whole>%<gray>.",
        1..5,
        ItemType.weapons(),
        multiplier = 1f,
        baseSpecials = specialStats {
            statBoost = 0.05f
        }
    )),

    ULTIMATE_WISE(UltimateWiseEnchantment),
    ULTIMATE_BULK(UltimateBulkEnchantment),

    ONE_FOR_ALL(OneForAllEnchantment)
    ;

    companion object {
        fun init() {
            for (ench in values()) {
                EnchantmentRegistry.register(Identifier.macro(ench.name.lowercase()), ench.enchant)
            }
        }
    }
}
