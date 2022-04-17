package space.maxus.macrocosm.item

import net.axay.kspigot.items.flags
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import java.util.concurrent.TimeUnit

enum class ItemValue(private val item: MacrocosmItem) {
    ASPECT_OF_THE_END(AbilityItem(ItemType.SWORD, "Aspect of the End", Rarity.RARE, Material.DIAMOND_SWORD, stats {
        damage = 100f
        strength = 50f
        intelligence = 150f
    }, mutableListOf(InstantTransmission)))

    ;

    companion object {
        fun enchanted(type: Material) = ItemRegistry.find("ENCHANTED_${type.name}")

        fun placeholder(type: Material) = itemStack(type) {
            meta {
                displayName(Component.empty())
                flags(*ItemFlag.values())
            }
        }

        fun placeholder(type: Material, name: String) = itemStack(type) {
            meta {
                displayName(comp(name))
                flags(*ItemFlag.values())
            }
        }

        private val allowedEnchantedMats = listOf(
            // mining
            "DIAMOND", "EMERALD", "LAPIS_LAZULI", "COAL", "REDSTONE", "COBBLESTONE", "IRON_INGOT", "GOLD_INGOT", "OBSIDIAN", "END_STONE", "NETHERITE_SCRAP", "QUARTZ",
            // foraging
            "OAK_LOG", "BIRCH_LOG", "SPRUCE_LOG", "ACACIA_LOG", "DARK_OAK_LOG", "JUNGLE_LOG", "WARPED_STEM", "CRIMSON_STEM",
            // farming
            "POTATO", "CARROT", "WHEAT", "RED_MUSHROOM", "BROWN_MUSHROOM", "CHORUS_FLOWER", "NETHER_WART", "BEETROOT", "SUGAR_CANE", "CACTUS", "MELON_SLICE", "PUMPKIN",
            "BAMBOO", "PORKCHOP", "BEEF", "CHICKEN", "RABBIT", "RABBIT_FOOT", "HONEYCOMB",
            // excavating
            "DIRT", "GRAVEL", "SAND", "CLAY_BALL", "SOUL_SAND", "SOUL_SOIL",
            // combat
            "ROTTEN_FLESH", "ENDER_PEARL", "STRING", "SPIDER_EYE", "PHANTOM_MEMBRANE", "GUNPOWDER", "GHAST_TEAR", "BONE", "SLIME_BALL", "MAGMA_CREAM", "BLAZE_POWDER",
            // fishing
            "COD", "SALMON", "SPONGE", "TROPICAL_FISH", "PRISMARINE_SHARD", "PRISMARINE_CRYSTALS", "INK_SAC", "PUFFERFISH", "KELP", "LILY_PAD",
            // more
            "NETHERITE_INGOT", "NETHERITE_BLOCK"
        )
        private fun initEnchanted() {
            val pool = Threading.pool()

            for(allowed in allowedEnchantedMats.parallelStream()) {
                pool.execute {
                    val mat = Material.valueOf(allowed)
                    val item = EnchantedItem(mat, rarityFromMaterial(mat).next())

                    ItemRegistry.register("ENCHANTED_$allowed", item)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if(!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }

        fun init() {
            Ability.init()

            Threading.start {
                initEnchanted()
            }

            // using thread pools to not create a bottleneck
            val pool = Threading.pool()

            for (item in values().toList().parallelStream()) {
                pool.execute {
                    ItemRegistry.register(item.name, item.item)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if(!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }
    }
}
