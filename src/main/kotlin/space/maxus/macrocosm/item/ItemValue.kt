package space.maxus.macrocosm.item

import org.bukkit.Material
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.stats.stats

enum class ItemValue(private val item: MacrocosmItem) {
    ASPECT_OF_THE_END(AbilityItem(ItemType.SWORD, "Aspect of the End", Rarity.RARE, Material.DIAMOND_SWORD, stats {
        damage = 100f
        strength = 50f
        intelligence = 150f
    }, mutableListOf(InstantTransmission)))

    ;

    companion object {
        fun enchanted(type: Material, rarity: Rarity, id: String? = null) = EnchantedItem(type, rarity, "Enchanted ${type.name.replace("_", " ").capitalized()}", id)

        fun init() {
            Ability.init()
            for (item in values()) {
                ItemRegistry.register(item.name, item.item)
            }
        }
    }
}
