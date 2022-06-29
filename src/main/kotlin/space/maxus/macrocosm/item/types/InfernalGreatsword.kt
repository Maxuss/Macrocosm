package space.maxus.macrocosm.item.types

import org.bukkit.Material
import space.maxus.macrocosm.ability.types.item.FierySlashAbility
import space.maxus.macrocosm.ability.types.item.InfernalGreatswordThrowAbility
import space.maxus.macrocosm.item.AbilityItem
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

class InfernalGreatsword: AbilityItem(
    ItemType.LONGSWORD,
    "Infernal Greatsword",
    Rarity.LEGENDARY,
    Material.GOLDEN_SWORD,
    stats {
        damage = 400f
        strength = 125f
        ferocity = 40f
    },
    mutableListOf(FierySlashAbility, InfernalGreatswordThrowAbility),
    description = "<gray>All <gold>Fire Damage<gray> inflicted by this <gray>sword deals <red>1.5x<gray> more <red>${Statistic.DAMAGE.display}<gray>.",
    runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.COMBAT)
)
