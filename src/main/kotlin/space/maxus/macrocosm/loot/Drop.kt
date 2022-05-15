package space.maxus.macrocosm.loot

import org.bukkit.Material
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.util.Identifier

abstract class Drop(val rarity: DropRarity, var chance: Double, val item: Identifier, val amount: IntRange)

class VanillaDrop(material: Material, amount: IntRange, rarity: DropRarity, chance: Double) :
    Drop(rarity, chance, Identifier("minecraft", material.name.lowercase()), amount)

class MacrocosmDrop(item: Identifier, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) :
    Drop(rarity, chance, item, amount)

fun vanilla(material: Material, chance: Double, rarity: DropRarity = DropRarity.COMMON, amount: IntRange = 1..1) =
    VanillaDrop(material, amount, rarity, chance)

fun custom(item: MacrocosmItem, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) =
    MacrocosmDrop(ItemRegistry.nameOf(item)!!, rarity, chance, amount)
