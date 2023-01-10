package space.maxus.macrocosm.slayer.zombie

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.EntitySoundBank
import space.maxus.macrocosm.entity.EntitySoundType
import space.maxus.macrocosm.item.ColoredEntityArmor
import space.maxus.macrocosm.item.SkullEntityHead
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.slayer.SlayerBase
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats

class RevenantHorror(stats: Statistics, tier: Int, exp: Double) : SlayerBase(
    EntityType.ZOMBIE,
    SlayerType.REVENANT_HORROR,
    tier,
    exp,
    stats,
    mainHand = VanillaItem(Material.DIAMOND_HOE),
    helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDhiZWUyM2I1YzcyNmFlOGUzZDAyMWU4YjRmNzUyNTYxOWFiMTAyYTRlMDRiZTk4M2I2MTQxNDM0OWFhYWM2NyJ9fX0="),
    chestplate = VanillaItem(Material.DIAMOND_CHESTPLATE),
    leggings = VanillaItem(Material.CHAINMAIL_LEGGINGS),
    boots = VanillaItem(Material.DIAMOND_BOOTS)
)

object AtonedHorror : SlayerBase(
    EntityType.ZOMBIE,
    SlayerType.REVENANT_HORROR,
    5,
    2000.0,
    stats {
        health = 5_000_000f
        defense = 1200f
        damage = 200f
        strength = 200f
        trueDefense = 700f
        ferocity = 200f
        speed = 280f
    },
    VanillaItem(Material.IRON_AXE),
    helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FmZGUyODRkN2M4ZDQ0YWE1OWIyNjdmNmYwODcxY2RjZWY5OTI2YzgxNjA3YWJiZGI2MWIxNGUxYjZhOTQyZiJ9fX0="),
    chestplate = ColoredEntityArmor(Material.LEATHER_CHESTPLATE, 0xFFFFFF),
    leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0xDBF9F4),
    boots = VanillaItem(Material.IRON_BOOTS),
    actualName = "<aqua>Atoned Horror"
)

object EntombedReaper : SlayerBase(
    EntityType.ZOMBIE,
    SlayerType.REVENANT_HORROR,
    6,
    25000.0,
    stats {
        health = 10_000_000f
        defense = 1700f
        damage = 400f
        strength = 300f
        trueDefense = 2100f
        ferocity = 300f
        speed = 200f
    },
    mainHand = VanillaItem(Material.NETHERITE_HOE),
    helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FmZGUyODRkN2M4ZDQ0YWE1OWIyNjdmNmYwODcxY2RjZWY5OTI2YzgxNjA3YWJiZGI2MWIxNGUxYjZhOTQyZiJ9fX0="),
    chestplate = VanillaItem(Material.NETHERITE_CHESTPLATE),
    leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x000000),
    boots = ColoredEntityArmor(Material.LEATHER_BOOTS, 0x000000),
    sounds = EntitySoundBank.from(
        EntitySoundType.DAMAGED to (Sound.ENTITY_PHANTOM_HURT to 0f),
        EntitySoundType.DEATH to (Sound.ENTITY_PHANTOM_DEATH to 0f)
    ),
    actualName = "<dark_aqua>Entombed Reaper"
)
