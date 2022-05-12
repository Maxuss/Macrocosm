package space.maxus.macrocosm.enchants.type

import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object OneForAllEnchantment: UltimateEnchantment(
    "One For All",
    "Removes <yellow>all<gray> enchantments, but boosts ${Statistic.DAMAGE.display}<gray> of this item by <red>500%<gray>.",
    1..1,
    ItemType.melee(),
    baseStats = stats {
        damageBoost = 500f
    },
    conflicts = listOf("ALL")
)
