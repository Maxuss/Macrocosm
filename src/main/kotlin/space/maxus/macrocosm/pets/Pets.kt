package space.maxus.macrocosm.pets

import org.bukkit.Material
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.SkillTable
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class PetValue(val pet: Pet) {
    TEST_PET(object: Pet(id("test_pet"), "Test Pet", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODIyMjkyZDkxODNlMzhmM2JlOWYwNmY5NjYzOTRlMmRhZmYzNjJmNzBhZTQ1Y2RlNDEyYjg3YmNkYjg5YzE1OCJ9fX0=") {
        override val effects: PetEffects = FixedPetEffects(listOf(BlockPetParticle(Material.SEA_PICKLE, 3)))
        override val table: LevelingTable = SkillTable
    })
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
