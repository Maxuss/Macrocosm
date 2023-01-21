package space.maxus.macrocosm.collections

import org.bukkit.Material
import space.maxus.macrocosm.collections.CollectionSection.*
import space.maxus.macrocosm.collections.table.CollectionTables.*
import space.maxus.macrocosm.registry.Identifier
import java.io.Serializable

/**
 * A type of collection, which is associated with items that are collected for this collection
 */
// todo: collection recipes
enum class CollectionType(val inst: Collection) : Serializable {
    // combat
    ROTTEN_FLESH(Collection("Rotten Flesh", listOf(), COMBAT, GENERIC_COMBAT)),
    BONE(Collection("Bone", listOf(), COMBAT, GENERIC_COMBAT)),
    SLIME_BALL(Collection("Slime Ball", listOf(), COMBAT, GENERIC_COMBAT)),
    STRING(Collection("String", listOf(), COMBAT, GENERIC_COMBAT)),
    GUNPOWDER(Collection("Gunpowder", listOf(), COMBAT, GREEDY_TABLE)),
    MAGMA_CREAM(Collection("Magma Cream", listOf(), COMBAT, GENERIC_COMBAT)),
    BLAZE_ROD(Collection("Blaze Rod", listOf(), COMBAT, GENERIC_COMBAT)),
    ENDER_PEARL(Collection("Ender Pearl", listOf(), COMBAT, GENERIC_COMBAT)),
    GHAST_TEAR(Collection("Ghast Tear", listOf(), COMBAT, GREEDY_TABLE)),

    // mining
    COBBLESTONE(Collection("Cobblestone", listOf(), MINING, GENERIC)),
    COAL(Collection("Coal", listOf(), MINING, GENERIC)),
    COPPER_INGOT(Collection("Copper Ingot", listOf(), MINING, GENERIC)),
    REDSTONE(Collection("Redstone", listOf(), MINING, COLOSSAL_TABLE)),
    GOLD_INGOT(Collection("Gold Ingot", listOf(), MINING, GENERIC)),
    IRON_INGOT(Collection("Iron Ingot", listOf(), MINING, GENERIC)),
    DIAMOND(Collection("Diamond", listOf(), MINING, STEEPER)),
    GEMSTONE(Collection("Gemstone", listOf(), MINING, COLOSSAL_TABLE)),
    EMERALD(Collection("Emerald", listOf(), MINING, GENERIC)),
    ICE(Collection("Ice", listOf(), MINING, COLOSSAL_TABLE)),
    END_STONE(Collection("Endstone", listOf(), MINING, GENERIC)),
    SILVER(Collection("Silver", listOf(), MINING, GREEDY_TABLE)),
    MITHRIL(Collection("Mithril", listOf(), MINING, GREEDY_TABLE)),
    ADAMANTITE(Collection("Adamantite", listOf(), MINING, GREEDY_TABLE)),
    TITANIUM(Collection("Titanium", listOf(), MINING, GREEDY_TABLE)),

    // farming
    WHEAT(Collection("Wheat", listOf(), FARMING, GREEDY_TABLE)),
    CARROT(Collection("Carrot", listOf(), FARMING, GREEDY_TABLE)),
    POTATO(Collection("Potato", listOf(), FARMING, GREEDY_TABLE)),
    SUGAR_CANE(Collection("Sugar Cane", listOf(), FARMING, GREEDY_TABLE)),
    CACTUS(Collection("Cactus", listOf(), FARMING, GREEDY_TABLE)),
    MUSHROOM(Collection("Mushroom", listOf(), FARMING, GREEDY_TABLE)),
    CHICKEN(Collection("Chicken", listOf(), FARMING, GREEDY_TABLE)),
    PORK(Collection("Pork", listOf(), FARMING, GREEDY_TABLE)),
    BEEF(Collection("Beef", listOf(), FARMING, GREEDY_TABLE)),
    MUTTON(Collection("Mutton", listOf(), FARMING, GREEDY_TABLE)),
    NETHER_WART(Collection("Nether Wart", listOf(), FARMING, LESS)),
    CHORUS(Collection("Chorus", listOf(), FARMING, GREEDY_TABLE)),

    // fishing
    COD(Collection("Cod", listOf(), FISHING, GENERIC_FISHING)),
    SALMON(Collection("Salmon", listOf(), FISHING, GENERIC_FISHING)),
    PUFFERFISH(Collection("Cod", listOf(), FISHING, FISHING_LEAST)),
    TROPICAL_FISH(Collection("Cod", listOf(), FISHING, FISHING_LEAST)),
    SPONGE(Collection("Cod", listOf(), FISHING, GREEDY_TABLE)),
    INK_SAC(Collection("Ink Sac", listOf(), FISHING, GREEDY_TABLE)),
    PRISMARINE(Collection("Prismarine", listOf(), FISHING, FISHING_PRISMARINE)),
    SEAWEED(Collection("Seaweed", listOf(), FISHING, GREEDY_TABLE)),
    MAGMAFISH(Collection("Magmafish", listOf(), FISHING, FISHING_MAGMA)),

    // foraging
    OAK(Collection("Oak", listOf(), FORAGING, GENERIC)),
    SPRUCE(Collection("Spruce", listOf(), FORAGING, GENERIC)),
    JUNGLE_WOOD(Collection("Jungle Wood", listOf(), FORAGING, GENERIC)),
    DARK_OAK(Collection("Dark Oak", listOf(), FORAGING, GENERIC)),
    BIRCH(Collection("Birch", listOf(), FORAGING, GENERIC)),
    CRIMSON_WOOD(Collection("Crimson Wood", listOf(), FORAGING, STEEPER)),
    WARPED_WOOD(Collection("Warped Wood", listOf(), FORAGING, STEEPER)),
    STEELWOOD(Collection("Steelwood", listOf(), FORAGING, GREEDY_FORAGING)),
    CRYSTALWOOD(Collection("Crystalwood", listOf(), FORAGING, GREEDY_FORAGING)),
    SHADEWOOD(Collection("Shadewood", listOf(), FORAGING, GREEDY_FORAGING)),

    // excavating
    DIRT(Collection("Dirt", listOf(), EXCAVATING, APPROX_EXCAVATING)),
    SAND(Collection("Sand", listOf(), EXCAVATING, APPROX_EXCAVATING)),
    GRAVEL(Collection("Gravel", listOf(), EXCAVATING, APPROX_EXCAVATING)),
    NYLIUM(Collection("Nylium", listOf(), EXCAVATING, APPROX_EXCAVATING)),
    NETHERRACK(Collection("Netherrack", listOf(), EXCAVATING, STEEPER)),
    MOONSTONE_DUST(Collection("Moonstone Dust", listOf(), EXCAVATING, GREEDY_TABLE)),

    ;

    companion object {
        private val matToColl = hashMapOf(
            Material.ROTTEN_FLESH to ROTTEN_FLESH,
            Material.BONE to BONE,
            Material.SLIME_BALL to SLIME_BALL,
            Material.SLIME_BLOCK to SLIME_BALL,
            Material.STRING to STRING,
            Material.GUNPOWDER to GUNPOWDER,
            Material.MAGMA_CREAM to MAGMA_CREAM,
            Material.BLAZE_ROD to BLAZE_ROD,
            Material.ENDER_PEARL to ENDER_PEARL,
            Material.GHAST_TEAR to GHAST_TEAR,

            Material.COBBLESTONE to COBBLESTONE,
            Material.STONE to COBBLESTONE,
            Material.COAL to COAL,
            Material.COPPER_INGOT to COPPER_INGOT,
            Material.REDSTONE to REDSTONE,
            Material.GOLD_INGOT to GOLD_INGOT,
            Material.IRON_INGOT to IRON_INGOT,
            Material.DIAMOND to DIAMOND,
            Material.EMERALD to EMERALD,
            Material.ICE to ICE,
            Material.BLUE_ICE to ICE,
            Material.PACKED_ICE to ICE,
            Material.END_STONE to END_STONE,

            Material.WHEAT to WHEAT,
            Material.CARROT to CARROT,
            Material.POTATO to POTATO,
            Material.SUGAR_CANE to SUGAR_CANE,
            Material.CACTUS to CACTUS,
            Material.MUSHROOM_STEM to MUSHROOM,
            Material.RED_MUSHROOM to MUSHROOM,
            Material.BROWN_MUSHROOM to MUSHROOM,
            Material.CHICKEN to CHICKEN,
            Material.PORKCHOP to PORK,
            Material.BEEF to BEEF,
            Material.MUTTON to MUTTON,
            Material.NETHER_WART to NETHER_WART,
            Material.NETHER_WART_BLOCK to NETHER_WART,
            Material.CHORUS_FRUIT to CHORUS,

            Material.COD to COD,
            Material.SALMON to SALMON,
            Material.PUFFERFISH to PUFFERFISH,
            Material.TROPICAL_FISH to TROPICAL_FISH,
            Material.SPONGE to SPONGE,
            Material.WET_SPONGE to SPONGE,
            Material.INK_SAC to INK_SAC,
            Material.PRISMARINE_CRYSTALS to PRISMARINE,
            Material.PRISMARINE_SHARD to PRISMARINE,
            Material.KELP to SEAWEED,

            Material.OAK_LOG to OAK,
            Material.SPRUCE_LOG to SPRUCE,
            Material.BIRCH_LOG to BIRCH,
            Material.DARK_OAK_LOG to DARK_OAK,
            Material.CRIMSON_STEM to CRIMSON_WOOD,
            Material.WARPED_STEM to WARPED_WOOD,

            Material.DIRT to DIRT,
            Material.SAND to SAND,
            Material.GRAVEL to GRAVEL,
            Material.CRIMSON_NYLIUM to NYLIUM,
            Material.WARPED_NYLIUM to NYLIUM,
            Material.NETHERRACK to NETHERRACK

        )

        /**
         * Converts the provided material to a collection type
         *
         * NOTE: In most use cases [CollectionType.fromIdentifier] should be called first because it calls this method under the hood anyway
         * but also covers the null-cases from this method
         *
         * @return null when no collection types match the material
         */
        fun from(mat: Material): CollectionType? {
            return matToColl[mat]
        }

        /**
         * Converts the provided identifier to a collection type
         *
         * NOTE: this should not be called together with [CollectionType.from] as it is called internally in this method
         */
        fun fromIdentifier(id: Identifier): CollectionType? {
            if (id.path.contains("enchanted_"))
                return try {
                    from(Material.valueOf(id.path.replace("enchanted_", "").uppercase()))
                } catch (e: IllegalArgumentException) {
                    return when (id.path.replace("enchanted_", "")) {
                        "magmafish" -> MAGMAFISH
                        "moonstone_dust" -> MOONSTONE_DUST
                        "steelwood" -> STEELWOOD
                        "crystalwood" -> CRYSTALWOOD
                        "shadewood" -> SHADEWOOD
                        "silver" -> SILVER
                        "mithril" -> MITHRIL
                        "adamantite" -> ADAMANTITE
                        "titanium" -> TITANIUM
                        else -> null
                    }
                }
            return when (id.path) {
                "magmafish" -> MAGMAFISH
                "moonstone_dust" -> MOONSTONE_DUST
                "steelwood" -> STEELWOOD
                "crystalwood" -> CRYSTALWOOD
                "shadewood" -> SHADEWOOD
                "silver" -> SILVER
                "mithril" -> MITHRIL
                "adamantite" -> ADAMANTITE
                "titanium" -> TITANIUM
                else -> null
            }
        }
    }
}
