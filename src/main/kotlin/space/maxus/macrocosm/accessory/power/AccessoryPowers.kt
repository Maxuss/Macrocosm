package space.maxus.macrocosm.accessory.power

import org.bukkit.Material
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.general.id

enum class AccessoryPowers(val power: AccessoryPower) {
    TEST_POWER(SimpleAccessoryPower("test_power", Material.DIAMOND, "Testing", stats {
        strength = 6f
        defense = 4f
        health = 7.5f
        intelligence = 5f
        ferocity = .5f
    })),
    TEST_POWER_2(SimpleAccessoryPower("test_power_2", Material.DANDELION, "Testing 2", stats {
        strength = 1f
        critDamage = 8.5f
        defense = 1f
    })),
    TEST_STONE_POWER(object: StoneAccessoryPower(
        "test_stone_power",
        "Very Epic",
        "Cool Stone",
        Rarity.EPIC,
        "Marvelous",
        15,
        "Does nothing special",
        "I have no idea tbh",
        stats {
            strength = 7.5f
            critDamage = 1f
            health = 2f
        },
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg5YmNlNzM2YTRmZDc5M2VlMmEzODI3NTZhMjUzZjkyY2E3ZDdlYWMwMzFlNDViYTk3YWQwNmNlODYzZGQ0YiJ9fX0="
    ) {
        override fun registerListeners() {
            // do nothing
        }
    })
    ;

    companion object {
        fun init() {
            Registry.ACCESSORY_POWER.delegateRegistration(values().map { id(it.name.lowercase()) to it.power })
        }
    }
}
