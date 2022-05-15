package space.maxus.macrocosm.reforge

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.types.PoisonousReforge
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.Identifier

enum class ReforgeType(val ref: Reforge) {
    SPICY(SimpleReforge("Spicy", ItemType.melee(), stats {
        strength = 4f
        critDamage = 5f
        critChance = 2f
        attackSpeed = 2f
    })),

    WITHERED(SimpleReforge("Withered", ItemType.melee(), stats {
        strength = 30f
        critDamage = -2f
        attackSpeed = 1f
    })),

    SILKY(SimpleReforge("Silky", ItemType.melee(), stats {
        strength = -2f
        critDamage = 20f
        attackSpeed = -1f
    })),

    POISONOUS(PoisonousReforge())

    ;

    companion object {
        fun init() {
            Threading.start("Reforge Registry", true) {
                info("Starting ${javaClass.simpleName} daemon...")
                for (reforge in ReforgeType.values()) {
                    info("Registering ${reforge.name} reforge")
                    ReforgeRegistry.register(Identifier.macro(reforge.name.lowercase()), reforge.ref)
                }
            }
        }
    }
}
