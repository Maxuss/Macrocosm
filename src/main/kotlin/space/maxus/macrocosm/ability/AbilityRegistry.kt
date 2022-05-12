package space.maxus.macrocosm.ability

import space.maxus.macrocosm.util.Identifier

@Suppress("unused")
object AbilityRegistry {
    private val abilities: HashMap<Identifier, ItemAbility> = hashMapOf()

    fun register(name: Identifier, ability: ItemAbility): ItemAbility {
        if (abilities.containsKey(name)) {
            return ability
        }
        abilities[name] = ability
        ability.registerListeners()
        return ability
    }

    fun find(name: Identifier) = abilities[name]

    fun nameOf(ability: ItemAbility) = abilities.filter { (_, v) -> v == ability }.map { (k, _) -> k }.firstOrNull()

}
