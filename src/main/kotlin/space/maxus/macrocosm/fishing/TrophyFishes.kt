package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class TrophyFishes(val fish: TrophyFish) {

    ;

    companion object {
        fun init() {
            Registry.TROPHY_FISH.delegateRegistration(values().map { id(it.name.lowercase()) to it.fish }) { id, v ->
                Registry.ITEM.register(id, v)
            }
        }
    }

}
