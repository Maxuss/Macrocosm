package space.maxus.macrocosm.slayer.zombie

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.item.SkullEntityHead
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.slayer.SlayerBase
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

class DefaultRevenant(stats: Statistics, pool: LootPool, exp: Double): SlayerBase(
    comp("<red>☠ Revenant Horror ☠"),
    EntityType.ZOMBIE,
    pool,
    exp,
    stats,
    mainHand = VanillaItem(Material.DIAMOND_HOE),
    helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDhiZWUyM2I1YzcyNmFlOGUzZDAyMWU4YjRmNzUyNTYxOWFiMTAyYTRlMDRiZTk4M2I2MTQxNDM0OWFhYWM2NyJ9fX0="),
    chestplate = VanillaItem(Material.DIAMOND_CHESTPLATE),
    leggings = VanillaItem(Material.CHAINMAIL_LEGGINGS),
    boots = VanillaItem(Material.DIAMOND_BOOTS)
)
