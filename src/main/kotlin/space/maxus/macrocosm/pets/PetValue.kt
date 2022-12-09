package space.maxus.macrocosm.pets

import space.maxus.macrocosm.pets.types.*
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class PetValue(val pet: Pet) {
    PICKLE_PET(TestPet),
    PET_PHOENIX(PhoenixPet),
    PET_BEE(BeePet),
    PET_ENDER_DRAGON(EnderDragonPet),
    PET_PYROCLASTIC_TOAD(PyroclasticToadPet),
    PET_WASP(WaspPet),
    PET_WITHER_PHOENIX(AshenAvolotlePet)
    ;

    companion object {
        fun init() {
            Registry.PET.delegateRegistration(values().map { id(it.name.lowercase()) to it.pet }) { _, pet ->
                pet.registerItem()
            }
        }
    }
}
