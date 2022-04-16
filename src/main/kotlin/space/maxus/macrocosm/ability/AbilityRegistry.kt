package space.maxus.macrocosm.ability

import org.bukkit.Bukkit
import space.maxus.macrocosm.Macrocosm

@Suppress("unused")
object AbilityRegistry {
    private val abilities: HashMap<String, ItemAbility> = hashMapOf()

    fun register(name: String, ability: ItemAbility): ItemAbility {
        if (abilities.containsKey(name)) {
            return ability
        }
        abilities[name] = ability
        Bukkit.getServer().pluginManager.registerEvents(ability, Macrocosm)
        return ability
    }

    fun find(name: String) = abilities[name]

    fun nameOf(ability: ItemAbility) = abilities.filter { (_, v) -> v == ability }.map { (k, _) -> k }.firstOrNull()

}
