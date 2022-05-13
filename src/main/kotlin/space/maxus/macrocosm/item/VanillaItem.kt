package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.specialStats
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.Identifier
import java.util.*

internal val SPLIT_THIS =
    listOf("HOE", "PICKAXE", "AXE", "SWORD", "SHOVEL", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS")

internal fun typeFromMaterial(mat: Material): ItemType {
    if (SPLIT_THIS.any { mat.name.contains(it) })
        return ItemType.valueOf(mat.name.split("_").last())
    return when (mat) {
        Material.ELYTRA -> ItemType.CLOAK
        Material.BOW, Material.CROSSBOW -> ItemType.BOW
        else -> ItemType.OTHER
    }
}

internal fun specialStatsFromMaterial(mat: Material) = specialStats {
    if (mat.name.contains("NETHERITE")) {
        fireResistance = 0.2f
        knockbackResistance = 0.1f
        statBoost = 0.01f
    } else if (mat.name.contains("DIAMOND")) {
        statBoost = 0.01f
    }
}

internal fun statsFromMaterial(mat: Material) = stats {
    when (mat) {
        // weapons

        // melee
        Material.WOODEN_SHOVEL, Material.WOODEN_HOE, Material.WOODEN_PICKAXE -> {
            damage = 5f
            miningSpeed = 35f
        }
        Material.STONE_SHOVEL -> damage = 5f
        Material.STONE_HOE, Material.STONE_PICKAXE -> {
            damage = 5f
            miningSpeed = 60f
        }
        Material.WOODEN_SWORD, Material.WOODEN_AXE -> damage = 10f
        Material.IRON_PICKAXE, Material.IRON_HOE, Material.IRON_SHOVEL -> {
            damage = 10f
            miningSpeed = 110f
        }
        Material.STONE_SWORD, Material.STONE_AXE -> {
            damage = 20f
            miningSpeed = 150f
        }
        Material.IRON_SWORD -> damage = 20f
        Material.IRON_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE -> {
            damage = 20f
            miningSpeed = 180f
        }
        Material.DIAMOND_SWORD -> damage = 40f
        Material.DIAMOND_AXE -> {
            damage = 40f
            miningSpeed = 180f
        }
        Material.NETHERITE_PICKAXE -> {
            damage = 50f
            miningSpeed = 350f
            miningFortune = 45f
        }
        Material.NETHERITE_HOE -> {
            damage = 50f
            miningSpeed = 350f
            farmingFortune = 30f
        }
        Material.NETHERITE_SHOVEL -> {
            damage = 50f
            miningSpeed = 350f
            excavatingFortune = 40f
        }
        Material.NETHERITE_SWORD -> {
            damage = 100f
            strength = 25f
            ferocity = 10f
        }
        Material.NETHERITE_AXE -> {
            damage = 100f
            miningSpeed = 350f
            foragingFortune = 15f
        }

        Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL -> {
            damage = 10f
            magicFind = 5f
            miningSpeed = 300f
        }
        Material.GOLDEN_SWORD -> {
            damage = 20f
            magicFind = 8f
        }
        Material.GOLDEN_AXE -> {
            damage = 20f
            magicFind = 8f
            miningSpeed = 300f
        }

        // ranged
        Material.BOW -> damage = 15f
        Material.CROSSBOW -> damage = 25f

        // throwable
        Material.SNOWBALL, Material.EGG -> damage = 1f


        // armor
        Material.LEATHER_BOOTS, Material.LEATHER_HELMET -> defense = 10f
        Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_HELMET, Material.LEATHER_LEGGINGS -> defense = 15f
        Material.CHAINMAIL_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET -> defense =
            20f
        Material.CHAINMAIL_CHESTPLATE, Material.IRON_BOOTS, Material.IRON_HELMET -> defense = 25f
        Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE -> defense = 30f
        Material.DIAMOND_BOOTS, Material.DIAMOND_HELMET -> {
            defense = 40f
            health = 10f
        }
        Material.DIAMOND_LEGGINGS -> {
            defense = 45f
            health = 15f
        }
        Material.DIAMOND_CHESTPLATE -> {
            defense = 50f
            health = 25f
        }
        Material.NETHERITE_BOOTS, Material.NETHERITE_HELMET -> {
            defense = 75f
            trueDefense = 1f
            strength = 5f
        }
        Material.NETHERITE_LEGGINGS, Material.NETHERITE_CHESTPLATE -> {
            defense = 100f
            trueDefense = 2f
            strength = 10f
        }
        Material.CARVED_PUMPKIN -> defense = 5f
        Material.ELYTRA -> {
            magicFind = 25f
            petLuck = 5f
        }

        // misc
        Material.AMETHYST_SHARD -> {
            magicFind = 1f
        }

        else -> {}
    }
}

internal fun rarityFromMaterial(mat: Material): Rarity {
    if (mat == Material.NETHERITE_BLOCK)
        return Rarity.EPIC
    val name = mat.name
    if (name.contains("DIAMOND") || name.contains("EMERALD") || name.contains("END") || name.contains("CANDLE"))
        return Rarity.UNCOMMON
    if (name.contains("NETHERITE"))
        return Rarity.RARE
    if (name.contains("SKULL") || name.contains("HEAD"))
        return Rarity.EPIC

    return when (mat) {
        Material.AMETHYST_BLOCK, Material.AMETHYST_CLUSTER -> Rarity.UNCOMMON
        Material.ELYTRA -> Rarity.EPIC
        Material.NETHER_STAR -> Rarity.MYTHIC
        Material.TOTEM_OF_UNDYING -> Rarity.LEGENDARY
        else -> Rarity.COMMON
    }
}
private val blacklist: List<String> = listOf("SWORD", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "INGOT", "SCRAP")
private fun bpFromMat(mat: Material): Int {
    val name = mat.name
    if(blacklist.any { name.contains(it) } || mat.isBlock)
        return 0
    if(name.contains("WOODEN"))
        return 1
    if(name.contains("STONE"))
        return 2
    if(name.contains("IRON"))
        return 3
    if(name.contains("GOLDEN"))
        return 2
    if(name.contains("DIAMOND"))
        return 5
    if(name.contains("NETHERITE"))
        return 6
    return 0
}

class VanillaItem(override val base: Material, override var amount: Int = 1) : MacrocosmItem {
    override var stats: Statistics = statsFromMaterial(base)
    override var specialStats: SpecialStatistics = specialStatsFromMaterial(base)
    override var stars: Int = 0
        set(value) {
            if(value > maxStars)
                return
            else field = value
        }
    override val id: Identifier = Identifier("minecraft", base.name.lowercase())
    override val type: ItemType = typeFromMaterial(base)

    override val name: Component = base.name.lowercase().split("_").joinToString(" ") { str ->
        str.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
    }.toComponent()

    override var rarity: Rarity = rarityFromMaterial(base)
    override var rarityUpgraded: Boolean = false
    override var reforge: Reforge? = null
    override var abilities: MutableList<ItemAbility> = mutableListOf()
    override var enchantments: HashMap<Enchantment, Int> = hashMapOf()
    override var breakingPower: Int = bpFromMat(base)

    @Suppress("UNCHECKED_CAST")
    override fun clone(): MacrocosmItem {
        val vanilla = VanillaItem(base)
        vanilla.stats = stats.clone()
        vanilla.specialStats = specialStats.clone()
        vanilla.rarity = rarity
        vanilla.rarityUpgraded = rarityUpgraded
        vanilla.reforge = reforge?.clone()
        vanilla.enchantments = enchantments.clone() as HashMap<Enchantment, Int>
        vanilla.amount = amount
        return vanilla
    }
}
