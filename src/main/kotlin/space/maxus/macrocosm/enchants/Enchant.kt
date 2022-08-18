package space.maxus.macrocosm.enchants

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.enchants.type.*
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.specialStats
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.general.id

enum class Enchant(val enchant: Enchantment) {
    SHARPNESS(
        SimpleEnchantment(
            "Sharpness",
            "Increases all damage you deal towards your enemies by [10]%<gray>.",
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
            dmgMultiplier = .2f
        )
    ),
    DRAGON_HUNTER(
        MobEnchantment(
            "Dragon Hunter",
            listOf(EntityType.ENDER_DRAGON),
            listOf(),
            1..6,
            dmgMultiplier = .1f,
            applicable = ItemType.weapons()
        )
    ),
    IMPALING(
        MobEnchantment(
            "Impaling",
            listOf(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.SQUID, EntityType.GLOW_SQUID),
            listOf(),
            levels = 1..5,
            dmgMultiplier = .15f,
            applicable = ItemType.weapons()
        )
    ),


    // stat enchants
    VICIOUS(
        SimpleEnchantment(
            "Vicious",
            "Increases your ${Statistic.FEROCITY.display}<gray> by <red>[4]<gray>, which allows you to sometimes strike enemies <blue>twice<gray>.",
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
            "Increases your ${Statistic.BONUS_ATTACK_SPEED.display}<gray> by <yellow>[5]<gray>, which allows you to attack your enemies <blue>more often<gray>.",
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
            "Increases your ${Statistic.CRIT_DAMAGE.display}<gray> by <blue>[10]<gray>, which allows you to do more critical hit damage.",
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
            "Increases your ${Statistic.CRIT_CHANCE.display}<gray> by <blue>[5]<gray>, which allows you to deal <blue>critical hits<gray> more often.",
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
            "Increases your <green>Bonus Knockback<gray> by <green>[10]%<gray>, which allows you to strike enemies further from you.",
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
            "Increases your ${Statistic.MAGIC_FIND.display}<gray> by <aqua>[3]<gray>, which grants higher chance of getting rare drops.",
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

    // tools

    // default stuff

    // farming
    TURBO_POTATOES(
        BlockTargetingEnchantment(
            "Turbo-Potatoes",
            ItemType.HOE,
            "<yellow>Potatoes<gray>",
            Statistic.FARMING_FORTUNE,
            15,
            Material.POTATOES
        )
    ),
    TURBO_CARROTS(
        BlockTargetingEnchantment(
            "Turbo-Carrots",
            ItemType.HOE,
            "<#FF6927>Carrots<gray>",
            Statistic.FARMING_FORTUNE,
            15,
            Material.CARROTS
        )
    ),
    TURBO_PUMPKINS(
        BlockTargetingEnchantment(
            "Turbo-Pumpkins",
            ItemType.HOE,
            "<gold>Pumpkins<gray>",
            Statistic.FARMING_FORTUNE,
            15,
            Material.PUMPKIN
        )
    ),
    TURBO_WARTS(
        BlockTargetingEnchantment(
            "Turbo-Warts",
            ItemType.HOE,
            "<red>Nether Warts<gray>",
            Statistic.FARMING_FORTUNE,
            15,
            Material.NETHER_WART
        )
    ),

    // mining
    HYPER_COAL(
        BlockTargetingEnchantment(
            "Hyper-Coal",
            ItemType.PICKAXE,
            "<dark_gray>Coal<gray>",
            Statistic.MINING_FORTUNE,
            20,
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE
        )
    ),
    HYPER_GOLD(
        BlockTargetingEnchantment(
            "Hyper-Gold",
            ItemType.PICKAXE,
            "<#FFA327>Gold<gray>",
            Statistic.MINING_FORTUNE,
            20,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE
        )
    ),
    HYPER_EMERALDS(
        BlockTargetingEnchantment(
            "Hyper-Emeralds",
            ItemType.PICKAXE,
            "<#42FF27>Emeralds<gray>",
            Statistic.MINING_FORTUNE,
            15,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE
        )
    ),
    HYPER_DIAMONDS(
        BlockTargetingEnchantment(
            "Hyper-Diamonds",
            ItemType.PICKAXE,
            "<#27FFC9>Diamonds<gray>",
            Statistic.MINING_FORTUNE,
            15,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE
        )
    ),

    // foraging
    SUPER_JUNGLE(
        BlockTargetingEnchantment(
            "Super-Jungle",
            ItemType.AXE,
            "<#FF917D>Jungle Wood<gray>",
            Statistic.FORAGING_FORTUNE,
            25,
            Material.JUNGLE_LOG
        )
    ),
    SUPER_OAK(
        BlockTargetingEnchantment(
            "Super-Oak",
            ItemType.AXE,
            "<white>Oak<gray> and <dark_gray>Dark Oak<gray> Wood",
            Statistic.FORAGING_FORTUNE,
            25,
            Material.DARK_OAK_LOG,
            Material.OAK_LOG
        )
    ),
    SUPER_BIRCH(
        BlockTargetingEnchantment(
            "Super-Birch",
            ItemType.AXE,
            "<#CCC9C8>Birch Wood<gray>",
            Statistic.FORAGING_FORTUNE,
            25,
            Material.BIRCH_LOG
        )
    ),
    SUPER_ACACIA(
        BlockTargetingEnchantment(
            "Super-Acacia",
            ItemType.AXE,
            "<#F9E5A5>Acacia Wood<gray>",
            Statistic.FORAGING_FORTUNE,
            25,
            Material.ACACIA_LOG
        )
    ),

    // excavating
    ULTRA_SAND(
        BlockTargetingEnchantment(
            "Ultra-Sand",
            ItemType.SHOVEL,
            "<#FFE592>Sand<gray> and <#9A1308>Soul Sand<gray>",
            Statistic.EXCAVATING_FORTUNE,
            30,
            Material.SAND,
            Material.SOUL_SAND
        )
    ),
    ULTRA_CLAY(
        BlockTargetingEnchantment(
            "Ultra-Clay",
            ItemType.SHOVEL,
            "<#BBFDE5>Clay<gray>",
            Statistic.EXCAVATING_FORTUNE,
            30,
            Material.CLAY
        )
    ),
    ULTRA_NYLIUM(
        BlockTargetingEnchantment(
            "Ultra-Nylium",
            ItemType.SHOVEL,
            "<#E7BBFD><obfuscated>a</obfuscated> Warped <obfuscated>a</obfuscated><gray> and <#FDBBBB><obfuscated>a</obfuscated> Crimson <obfuscated>a</obfuscated><gray> Nylium",
            Statistic.EXCAVATING_FORTUNE,
            30,
            Material.CRIMSON_NYLIUM,
            Material.WARPED_NYLIUM
        )
    ),
    ULTRA_GRAVEL(
        BlockTargetingEnchantment(
            "Ultra-Gravel",
            ItemType.SHOVEL,
            "<#808080>Gravel<gray>",
            Statistic.EXCAVATING_FORTUNE,
            30,
            Material.GRAVEL
        )
    ),

    // armor
    GROWTH(
        SimpleEnchantment(
            "Growth",
            "Increases your ${Statistic.HEALTH.display}<gray> by <green>[25]<gray>.",
            1..7,
            ItemType.armor(),
            stats {
                health = 25f
            })
    ),
    PROTECTION(
        SimpleEnchantment(
            "Protection",
            "Increases your ${Statistic.DEFENSE.display}<gray> by <green>[15]<gray>.",
            1..7,
            ItemType.armor(),
            stats {
                defense = 15f
            })
    ),
    TRUE_PROTECTION(
        SimpleEnchantment(
            "True Protection",
            "Increases your ${Statistic.TRUE_DEFENSE.display}<gray> by <green>[5]<gray>.",
            1..3,
            listOf(ItemType.CHESTPLATE),
            stats {
                trueDefense = 5f
            },
            conflicts = listOf("DISTURBANCE")
        )
    ),
    DISTURBANCE(
        SimpleEnchantment(
            "Disturbance",
            "Increases your ${Statistic.STRENGTH.display}<gray> by <green>[10]<gray>.",
            1..2,
            listOf(ItemType.CHESTPLATE),
            stats {
                strength = 10f
            })
    ),

    // ultimate
    SUPERIOR(UltimateEnchantment(
        "Superior",
        "Increases <blue>ALL<gray> your stats by <red>[5]%<gray>.",
        1..5,
        ItemType.weapons(),
        multiplier = 1f,
        baseSpecials = specialStats {
            statBoost = 0.05f
        }
    )),

    ULTIMATE_WISE(UltimateWiseEnchantment),
    ULTIMATE_BULK(UltimateBulkEnchantment),
    ULTIMATE_CONTROL(UltimateControlEnchantment),

    ONE_FOR_ALL(OneForAllEnchantment),
    LEGION(LegionEnchantment)
    ;

    companion object {
        fun init() {
            Registry.ENCHANT.delegateRegistration(values().map { id(it.name.lowercase()) to it.enchant })
        }
    }
}
