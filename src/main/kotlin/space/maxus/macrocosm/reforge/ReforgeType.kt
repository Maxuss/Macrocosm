package space.maxus.macrocosm.reforge

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.types.PoisonousReforge
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.Identifier

enum class ReforgeType(private val ref: Reforge) {
    SPICY(SimpleReforge("Spicy", ItemType.melee(), stats {
        strength = 4f
        critDamage = 5f
        critChance = 2f
        attackSpeed = 2f
    })),

    POISONOUS(PoisonousReforge())

    ;

    companion object {
        fun init() {
            for (reforge in ReforgeType.values()) {
                ReforgeRegistry.register(Identifier.macro(reforge.name.lowercase()), reforge.ref)
            }
        }
    }
}
