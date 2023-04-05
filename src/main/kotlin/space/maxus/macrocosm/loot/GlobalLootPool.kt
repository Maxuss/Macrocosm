package space.maxus.macrocosm.loot

import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.PetType
import space.maxus.macrocosm.players.MacrocosmPlayer

object GlobalLootPool {
    // suppressing because will be used later
    @Suppress("UNUSED_PARAMETER")
    fun of(player: MacrocosmPlayer, entity: MacrocosmEntity): LootPool {
        return LootPool.of(
            pet(PetType.PET_PHOENIX.pet, Rarity.EPIC, DropRarity.INSANE, 1 / 500_000.0),
            pet(PetType.PET_PHOENIX.pet, Rarity.LEGENDARY, DropRarity.UNBELIEVABLE, 1 / 3_000_000.0 /* 1.0 */)
        )
    }
}
