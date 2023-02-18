package space.maxus.macrocosm.achievement

import org.bukkit.Material

enum class AchievementRarity(val closedDisplay: Material, val openDisplay: Material, val hasGlint: Boolean = false) {
    // Basic achievements that you get by doing the storyline
    BASIC(Material.COAL, Material.DIAMOND),
    // More rare and harder to get achievements
    RARE(Material.COAL, Material.EMERALD),
    // Very hard to get
    EPIC(Material.COAL_BLOCK, Material.DIAMOND_BLOCK, true),
    // Final achievements and the hardest ones
    MACROCOSMIC(Material.BLACK_CONCRETE, Material.AMETHYST_BLOCK, true)
}
