package space.maxus.macrocosm.accessory.power

import org.bukkit.Material
import space.maxus.macrocosm.accessory.power.impl.*
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.general.id

/**
 * Enum for all pre-made accessory powers
 */
enum class AccessoryPowers(val power: AccessoryPower) {
    // Starter
    FORTUITOUS(SimpleAccessoryPower(
        "fortuitous",
        Material.GOLD_INGOT,
        "Fortuitous",
        stats {
            critChance = 2f
            critDamage = 2.5f
            health = 4f
            defense = .5f
            strength = 2.5f
        }
    )),
    PRETTY(SimpleAccessoryPower(
        "pretty",
        Material.DANDELION,
        "Pretty",
        stats {
            critChance = .5f
            speed = .3f
            critDamage = 2f
            health = .5f
            defense = .5f
            strength = 2.5f
            intelligence = 4.5f
        }
    )),
    PROTECTED(SimpleAccessoryPower(
        "protected",
        Material.IRON_CHESTPLATE,
        "Protected",
        stats {
            critChance = .3f
            critDamage = .5f
            health = 5f
            defense = 5.5f
            strength = 1.2f
        }
    )),
    SIMPLE(SimpleAccessoryPower(
        "simple",
        Material.STONE,
        "Simple",
        stats {
            critChance = .7f
            speed = .6f
            critDamage = 1.5f
            health = 2.5f
            defense = 1.5f
            strength = 1.8f
            intelligence = 3f
        }
    )),
    WARRIOR(SimpleAccessoryPower(
        "warrior",
        Material.DIAMOND,
        "Warrior",
        stats {
            critChance = 1.3f
            critDamage = 3.1f
            health = 1.7f
            defense = .5f
            strength = 4.5f
        }
    )),

    // Intermediate
    INSPIRED(
        SimpleAccessoryPower(
            "inspired",
            Material.LAPIS_LAZULI,
            "Inspired",
            stats {
                critChance = .5f
                critDamage = 2f
                health = 1f
                defense = .5f
                strength = 2f
                intelligence = 7f
            },
            "Intermediate"
        )
    ),
    OMINOUS(
        SimpleAccessoryPower(
            "ominous",
            Material.PRISMARINE_CRYSTALS,
            "Ominous",
            stats {
                critChance = .7f
                speed = .5f
                attackSpeed = .7f
                critDamage = 2f
                health = 2.5f
                strength = 2f
                intelligence = 2f
            },
            "Intermediate"
        )
    ),

    // Stones
    SILKY(Silky),
    BLOODY(Bloody),
    BLAZING(Blazing),
    FREEZING(Freezing),
    WARPING(Warping)

    ;

    companion object {
        fun init() {
            Registry.ACCESSORY_POWER.delegateRegistration(values().map { id(it.name.lowercase()) to it.power })
        }
    }
}
