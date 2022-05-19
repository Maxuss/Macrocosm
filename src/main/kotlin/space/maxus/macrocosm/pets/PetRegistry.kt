package space.maxus.macrocosm.pets

import net.axay.kspigot.extensions.pluginManager
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object PetRegistry {
    val pets: ConcurrentHashMap<Identifier, Pet> = ConcurrentHashMap(hashMapOf())

    fun register(id: Identifier, pet: Pet): Pet {
        if(pets.containsKey(id))
            return pets[id]!!
        pets[id] = pet
        pluginManager.registerEvents(pet, Macrocosm)
        return pet
    }

    fun find(id: Identifier) = pets[id]!!
}
