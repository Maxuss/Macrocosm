package space.maxus.macrocosm.accessory.power

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.general.id

enum class AccessoryPowers(val power: AccessoryPower) {
    TEST_POWER(SimpleAccessoryPower("test_power", "Testing", stats {
        strength = 6f
        defense = 4f
        health = 7.5f
        intelligence = 5f
        ferocity = .5f
    }))
    ;

    companion object {
        fun init() {
            Registry.ACCESSORY_POWER.delegateRegistration(values().map { id(it.name.lowercase()) to it.power })
        }
    }
}
