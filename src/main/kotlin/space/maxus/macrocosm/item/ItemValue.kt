package space.maxus.macrocosm.item

import org.bukkit.Material
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.stats.stats

enum class ItemValue(private val item: MacrocosmItem) {
    ASPECT_OF_THE_END(AbilityItem(ItemType.SWORD, "Aspect of the End", Rarity.RARE, Material.DIAMOND_SWORD, stats {
        damage = 100f
        strength = 50f
        intelligence = 150f
    }, mutableListOf(InstantTransmission)))

    ;

    companion object {
        fun init() {
            Ability.init()
            for (item in values()) {
                ItemRegistry.register(item.name, item.item)
            }
        }
    }
}
