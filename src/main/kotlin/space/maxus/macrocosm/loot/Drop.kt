package space.maxus.macrocosm.loot

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.ItemPet
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.pets.Pet
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.math.Chance

abstract class Drop(val rarity: DropRarity, override val chance: Double, val item: Identifier, var amount: IntRange) :
    Chance {
    fun clone(): Drop {
        return MacrocosmDrop(item, rarity, chance, amount)
    }

    fun dropRngesusReward(player: MacrocosmPlayer): ItemStack {
        return if (this.item.namespace == "minecraft") {
            val mat = Material.valueOf(this.item.path.uppercase())
            val amount = this.amount.random()
            val item = ItemStack(mat, amount)
            if (player.paper != null) {
                this.rarity.announceEntityDrop(player.paper!!, item.macrocosm!!, this, true)
            }
            item.macrocosm?.build(player)!!
        } else if (this.item.path.contains("pet")) {
            val (id, rarity) = this.item.path.split("@")
            val newId = id(id)
            val basePet = Registry.ITEM.find(newId) as ItemPet
            val rar = Rarity.valueOf(rarity.uppercase())
            basePet.stored = StoredPet(newId, rar, 1, .0)
            basePet.rarity = rar
            if (player.paper != null) {
                this.rarity.announceEntityDrop(player.paper!!, basePet, this, true)
            }
            basePet.build(player)!!
        } else {
            val item = Registry.ITEM.find(this.item)
            val amount = this.amount.random()
            if (player.paper != null) {
                this.rarity.announceEntityDrop(player.paper!!, item, this, true)
            }
            item.build(player)!!.apply { this.amount = amount }
        }
    }
}

class VanillaDrop(material: Material, amount: IntRange, rarity: DropRarity, chance: Double) :
    Drop(rarity, chance, Identifier("minecraft", material.name.lowercase()), amount)

class MacrocosmDrop(item: Identifier, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) :
    Drop(rarity, chance, item, amount)

fun vanilla(material: Material, chance: Double, rarity: DropRarity = DropRarity.COMMON, amount: IntRange = 1..1) =
    VanillaDrop(material, amount, rarity, chance)

fun custom(item: MacrocosmItem, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) =
    MacrocosmDrop(Registry.ITEM.byValue(item)!!, rarity, chance, amount)

fun custom(item: Identifier, rarity: DropRarity, chance: Double, amount: IntRange = 1..1) =
    MacrocosmDrop(item, rarity, chance, amount)

fun pet(pet: Pet, rarity: Rarity, dropRarity: DropRarity, chance: Double) =
    MacrocosmDrop(id("${pet.id.path}@${rarity.name.lowercase()}"), dropRarity, chance)
