package space.maxus.macrocosm.bazaar

import org.bukkit.Material
import space.maxus.macrocosm.util.general.varargs

enum class BazaarCategory(val displayName: String, val description: String, val outline: Material, val displayItem: Material, vararg items: BazaarCollection) {
    // todo: add collections
    MINING("<aqua>Mining", "Contains all mining related ores and resources.", Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.IRON_PICKAXE),
    COMBAT("<gold>Combat", "Contains all combat related mob drops and resources.", Material.ORANGE_STAINED_GLASS_PANE, Material.IRON_SWORD),
    FORAGING("<dark_green>Foraging", "Contains all foraging related logs and resources.", Material.GREEN_STAINED_GLASS_PANE, Material.IRON_AXE),
    EXCAVATING("<yellow>Excavating", "Contains all excavating related resources.", Material.YELLOW_STAINED_GLASS_PANE, Material.IRON_SHOVEL),
    FISHING("<blue>Fishing", "Contains all fishing related resources.", Material.BLUE_STAINED_GLASS_PANE, Material.FISHING_ROD),
    ODDITIES("<light_purple>Oddities", "Contains all kinds of interesting items and resources.", Material.PINK_STAINED_GLASS_PANE, Material.ENCHANTING_TABLE),

    ;

    val items by varargs(items)
}
