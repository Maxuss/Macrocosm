package space.maxus.macrocosm.pets

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.pets.types.PhoenixPet
import space.maxus.macrocosm.pets.types.TestPet
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class PetValue(val pet: Pet) {
    PICKLE_PET(TestPet),
    PET_PHOENIX(PhoenixPet)
    ;

    companion object {
        fun init() {
            Threading.start("Pet Registry Daemon") {
                info("Starting Pet Registry daemon...")

                val pool = Threading.pool()
                for (pet in values()) {
                    pool.execute {
                        val id = id(pet.name.lowercase())
                        PetRegistry.register(id, pet.pet)
                        pet.pet.registerItem()
                    }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")

                info("Successfully registered ${EntityValue.values().size} pets")
            }
        }
    }
}
