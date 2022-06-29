package space.maxus.macrocosm.item.types

import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.item.ArmorItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.colorMeta
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.stats.Statistics

class DragonArmor(
    name: String,
    id: String,
    stats: Statistics,
    headSkin: String,
    chestColor: Int,
    legsColor: Int,
    bootsColor: Int,
    rarity: Rarity = Rarity.LEGENDARY,
    abilities: MutableList<MacrocosmAbility> = mutableListOf(),
    applicableRuns: MutableList<RuneSlot> = mutableListOf(),
): ArmorItem(
    "$name Dragon",
    id,
    "LEATHER",
    rarity,
    stats,
    abilities = abilities,
    headSkin = headSkin,
    chestMeta = colorMeta(chestColor),
    legsMeta = colorMeta(legsColor),
    bootMeta = colorMeta(bootsColor),
    runes = applicableRuns
)
