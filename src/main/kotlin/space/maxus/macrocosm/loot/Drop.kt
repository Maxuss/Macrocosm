package space.maxus.macrocosm.loot

import org.bukkit.Material
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.Pet
import space.maxus.macrocosm.util.Chance
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.id

abstract class Drop(val rarity: DropRarity, override val chance: Double, val item: Identifier, val amount: IntRange): Chance

class VanillaDrop(material: Material, amount: IntRange, rarity: DropRarity, chance: Double) :
    Drop(rarity, chance, Identifier("minecraft", material.name.lowercase()), amount)

class MacrocosmDrop(item: Identifier, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) :
    Drop(rarity, chance, item, amount)

fun vanilla(material: Material, chance: Double, rarity: DropRarity = DropRarity.COMMON, amount: IntRange = 1..1) =
    VanillaDrop(material, amount, rarity, chance)

fun custom(item: MacrocosmItem, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) =
    MacrocosmDrop(ItemRegistry.nameOf(item)!!, rarity, chance, amount)

fun pet(pet: Pet, rarity: Rarity, dropRarity: DropRarity, chance: Double) =
    MacrocosmDrop(id("${pet.id.path}@${rarity.name.lowercase()}"), dropRarity, chance)
