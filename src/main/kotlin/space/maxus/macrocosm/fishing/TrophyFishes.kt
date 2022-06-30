package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.generic.id

enum class TrophyFishes(val fish: TrophyFish) {
//    TESTFIN(TrophyFish(
//        "Testfin",
//        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGM0ZDQ0YTBmZmUwMjM0OWU5OWRhMDYyOTIxMzA2MzExM2U2YmIzYWZjMjU5ZjQ2NjE4YzkwZWRjZTgzMDc4NiJ9fX0=",
//        CatchConditions("Catched by doing pretty <red>sussy<gray> stuff.", Predicates.alwaysTrue(), 1.0f),
//        Rarity.SPECIAL
//    ))

    ;

    companion object {
        fun init() {
            Registry.TROPHY_FISH.delegateRegistration(values().map { id(it.name.lowercase()) to it.fish }) { id, v ->
                Registry.ITEM.register(id, v)
            }
        }
    }

}
