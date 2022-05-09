package space.maxus.macrocosm.entity.loot

import org.bukkit.Material
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.util.Identifier

abstract class EntityDrop(val rarity: DropRarity, var chance: Double, val item: Identifier, val amount: IntRange)

class VanillaDrop(material: Material, amount: IntRange, rarity: DropRarity, chance: Double) :
    EntityDrop(rarity, chance, Identifier("minecraft", material.name.lowercase()), amount)

class MacrocosmDrop(item: Identifier, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) :
    EntityDrop(rarity, chance, item, amount)

fun vanilla(material: Material, chance: Double, rarity: DropRarity = DropRarity.COMMON, amount: IntRange = 1..1) =
    VanillaDrop(material, amount, rarity, chance)

fun custom(item: MacrocosmItem, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) =
    MacrocosmDrop(ItemRegistry.nameOf(item)!!, rarity, chance, amount)
