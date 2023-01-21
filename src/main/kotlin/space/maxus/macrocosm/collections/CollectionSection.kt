package space.maxus.macrocosm.collections

import org.bukkit.Material
import java.io.Serializable

/**
 * A section for collections
 */
enum class CollectionSection(val mat: Material) : Serializable {
    COMBAT(Material.STONE_SWORD),
    FARMING(Material.GOLDEN_HOE),
    FISHING(Material.FISHING_ROD),
    MINING(Material.STONE_PICKAXE),
    FORAGING(Material.JUNGLE_SAPLING),
    EXCAVATING(Material.STONE_SHOVEL)
}
