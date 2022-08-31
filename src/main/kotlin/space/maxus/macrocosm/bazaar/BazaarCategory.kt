package space.maxus.macrocosm.bazaar

import org.bukkit.Material
import space.maxus.macrocosm.util.general.varargs
import space.maxus.macrocosm.util.toList

enum class BazaarCategory(
    val displayName: String,
    val description: String,
    val outline: Material,
    val displayItem: Material,
    vararg items: BazaarCollection
) {
    MINING(
        "<aqua>Mining",
        "Contains all mining related ores and resources.",
        Material.LIGHT_BLUE_STAINED_GLASS_PANE,
        Material.IRON_PICKAXE,
        *(BazaarCollection.COBBLESTONE..BazaarCollection.ICE).toArray()
    ),
    COMBAT(
        "<red>Combat",
        "Contains all combat related mob drops and resources.",
        Material.RED_STAINED_GLASS_PANE,
        Material.IRON_SWORD,
        *(BazaarCollection.ROTTEN_FLESH..BazaarCollection.CINDERFLAME_SPIRIT).toArray()
    ),
    WOOD_AND_FISHES(
        "<dark_green>Wood and Fishes",
        "Contains all foraging and fishing related resources.",
        Material.GREEN_STAINED_GLASS_PANE,
        Material.IRON_AXE,
        *(BazaarCollection.OAK..BazaarCollection.SPONGE).toArray()
    ),
    EXCAVATING(
        "<gold>Excavating",
        "Contains all excavating related resources.",
        Material.ORANGE_STAINED_GLASS_PANE,
        Material.IRON_SHOVEL,
        *(BazaarCollection.NETHERRACK..BazaarCollection.SCULK).toArray()
    ),
    FARMING(
        "<yellow>Farming",
        "Contains all farming related crops and resources",
        Material.YELLOW_STAINED_GLASS_PANE,
        Material.IRON_HOE,
        *(BazaarCollection.WHEAT..BazaarCollection.HONEY).toArray(),
    ),
    ODDITIES(
        "<light_purple>Oddities",
        "Contains all kinds of interesting items and resources.",
        Material.PINK_STAINED_GLASS_PANE,
        Material.ENCHANTING_TABLE
    ),

    ;

    val items by varargs(items)
}

private fun ClosedRange<BazaarCollection>.toArray() = toList(BazaarCollection::values).toTypedArray()
