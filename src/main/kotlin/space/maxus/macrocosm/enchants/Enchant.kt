package space.maxus.macrocosm.enchants

import org.bukkit.entity.EntityType
import space.maxus.macrocosm.enchants.type.*
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

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

    // <stat>-steal enchants
    LIFE_STEAL(
        StealingEnchantment(
            "Life Steal",
            Statistic.HEALTH,
            0.5f,
            regain = { player, (amount, _) ->
                player.currentHealth = kotlin.math.min(player.currentHealth + amount, player.calculateStats()!!.health)
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
                    kotlin.math.min(player.currentMana + amount, player.calculateStats()!!.intelligence)
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
                    val stats = player.calculateStats()!!
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
    ;

    companion object {
        fun init() {
            for (ench in values()) {
                EnchantmentRegistry.register(ench.name, ench.enchant)
            }
        }
    }
}
