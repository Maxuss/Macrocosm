package space.maxus.macrocosm.reforge

import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.types.*
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.id

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

    POISONOUS(PoisonousReforge),
    FABLED(FabledReforge),

    BULKY(SimpleReforge("Bulky", ItemType.armor(), stats {
        speed = -2f
        health = 50f
    })),
    HEAVY(SimpleReforge("Heavy", ItemType.armor(), stats {
        speed = -2f
        defense = 20f
    })),
    REFRACTING(SimpleReforge("Refracting", ItemType.armor(), stats {
        speed = 5f
        intelligence = 25f
        abilityDamage = 1f
    })),
    NECROTIC(SimpleReforge("Necrotic", ItemType.armor(), stats {
        intelligence = 20f
        abilityDamage = 1f
    })),
    ORNATE(SimpleReforge("Ornate", ItemType.armor(), stats {
        intelligence = 10f
        strength = 5f
        defense = 10f
        critChance = 3f
    })),

    HEROIC(SimpleReforge("Heroic", ItemType.melee(), stats {
        intelligence = 20f
        strength = 5f
        attackSpeed = 2f
    })),

    SALTY(SimpleReforge("Salty", listOf(ItemType.FISHING_ROD), stats {
        seaCreatureChance = 2f
    })),
    TREACHEROUS(SimpleReforge("Treacherous", listOf(ItemType.FISHING_ROD), stats {
        treasureChance = 2f
    })),
    WASHED_UP(SimpleReforge("Washed-Up", listOf(ItemType.FISHING_ROD), stats {
        seaCreatureChance = 3f
        treasureChance = 3f
    })),
    FOAMY(SimpleReforge("Foamy", listOf(ItemType.FISHING_ROD), stats {
        seaCreatureChance = 5f
        treasureChance = -2f
        strength = 5f
    })),
    ABUNDANT(SimpleReforge("Abundant", listOf(ItemType.FISHING_ROD), stats {
        treasureChance = 5f
        seaCreatureChance = -2f
        intelligence = 5f
    })),
    SEABORN(SimpleReforge("Seaborn", ItemType.armor(), stats {
        seaCreatureChance = .5f
        critDamage = 5f
        strength = 5f
    })),
    MOURNING(SimpleReforge("Mourning", ItemType.armor(), stats {
        treasureChance = .5f
        intelligence = 10f
    })),

    UNDULANT(UndulantReforge),
    RELENTLESS(RelentlessReforge),
    RENOWNED(RenownedReforge)

    ;

    companion object {
        fun init() {
            Registry.REFORGE.delegateRegistration(values().map { id(it.name.lowercase()) to it.ref })
        }
    }
}
