package space.maxus.macrocosm.pets

import org.bukkit.entity.ArmorStand
import space.maxus.macrocosm.loot.DropRarity

val PET_DROP: DropRarity = DropRarity(broadcast = true, greet = true, name = "<gold>PET")

internal fun petTick() = { pet: ArmorStand ->
    pet.location
}
