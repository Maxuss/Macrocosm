package space.maxus.macrocosm.enchants

import org.bukkit.entity.EntityType
import space.maxus.macrocosm.enchants.type.MobEnchantment
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.stats

enum class Enchant(private val enchant: Enchantment) {
    SHARPNESS(SimpleEnchantment(
        "Sharpness",
        "Increases all damage you deal towards your enemies by <damage_boost>%<gray>.",
        1..7,
        ItemType.melee(),
        stats {
            damageBoost = 0.1f
        },
        conflicts = listOf("BANE_OF_ARTHROPODS", "SMITE", "COLD_TOUCH", "CUBISM", "ENDER_SLAYER"),
    )),

    // mob enchantments
    SMITE(MobEnchantment(
        "Smite",
        listOf(EntityType.ZOMBIE, EntityType.DROWNED, EntityType.HUSK, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN),
        listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "COLD_TOUCH", "CUBISM", "ENDER_SLAYER")
    )),
    BANE_OF_ARTHROPODS(MobEnchantment(
        "Bane of Arthropods",
        listOf(EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH, EntityType.ENDERMITE),
        listOf("SHARPNESS", "SMITE", "COLD_TOUCH", "CUBISM", "ENDER_SLAYER")
    )),
    COLD_TOUCH(MobEnchantment(
        "Cold Touch",
        listOf(EntityType.BLAZE, EntityType.GHAST, EntityType.MAGMA_CUBE),
        listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "SMITE", "CUBISM", "ENDER_SLAYER")
    )),
    CUBISM(MobEnchantment(
        "Cubism",
        listOf(EntityType.SLIME, EntityType.MAGMA_CUBE, EntityType.CREEPER, EntityType.STRIDER),
        listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "COLD_TOUCH", "SMITE", "ENDER_SLAYER")
    )),
    ENDER_SLAYER(MobEnchantment(
        "Ender Slayer",
        listOf(EntityType.ENDERMAN, EntityType.ENDERMITE),
        listOf("SHARPNESS", "BANE_OF_ARTHROPODS", "COLD_TOUCH", "CUBISM", "SMITE")
    ))
    ;

    companion object {
        fun init() {
            for (ench in values()) {
                EnchantmentRegistry.register(ench.name, ench.enchant)
            }
        }
    }
}
