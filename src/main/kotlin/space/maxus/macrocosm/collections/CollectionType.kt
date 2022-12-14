package space.maxus.macrocosm.collections

import org.bukkit.Material
import space.maxus.macrocosm.collections.Section.*
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.math.SkillTable
import java.io.Serializable

/**
 * A type of collection, which is associated with items that are collected for this collection
 */
// todo: collection recipes
// todo: implement an actual collection leveling table instead of a SkillTable
enum class CollectionType(val inst: Collection) : Serializable {
    // combat
    ROTTEN_FLESH(Collection("Rotten Flesh", listOf(), COMBAT, SkillTable)),
    BONE(Collection("Bone", listOf(), COMBAT, SkillTable)),
    SLIME_BALL(Collection("Slime Ball", listOf(), COMBAT, SkillTable)),
    STRING(Collection("String", listOf(), COMBAT, SkillTable)),
    GUNPOWDER(Collection("Gunpowder", listOf(), COMBAT, SkillTable)),
    MAGMA_CREAM(Collection("Magma Cream", listOf(), COMBAT, SkillTable)),
    BLAZE_ROD(Collection("Blaze Rod", listOf(), COMBAT, SkillTable)),
    ENDER_PEARL(Collection("Ender Pearl", listOf(), COMBAT, SkillTable)),
    GHAST_TEAR(Collection("Ghast Tear", listOf(), COMBAT, SkillTable)),

    // mining
    COBBLESTONE(Collection("Cobblestone", listOf(), MINING, SkillTable)),
    COAL(Collection("Coal", listOf(), MINING, SkillTable)),
    COPPER_INGOT(Collection("Copper Ingot", listOf(), MINING, SkillTable)),
    REDSTONE(Collection("Redstone", listOf(), MINING, SkillTable)),
    GOLD_INGOT(Collection("Gold Ingot", listOf(), MINING, SkillTable)),
    IRON_INGOT(Collection("Iron Ingot", listOf(), MINING, SkillTable)),
    DIAMOND(Collection("Diamond", listOf(), MINING, SkillTable)),
    GEMSTONE(Collection("Gemstone", listOf(), MINING, SkillTable)),
    EMERALD(Collection("Emerald", listOf(), MINING, SkillTable)),
    ICE(Collection("Ice", listOf(), MINING, SkillTable)),
    END_STONE(Collection("Endstone", listOf(), MINING, SkillTable)),
    SILVER(Collection("Silver", listOf(), MINING, SkillTable)),
    MITHRIL(Collection("Mithril", listOf(), MINING, SkillTable)),
    ADAMANTITE(Collection("Adamantite", listOf(), MINING, SkillTable)),
    TITANIUM(Collection("Titanium", listOf(), MINING, SkillTable)),

    // farming
    WHEAT(Collection("Wheat", listOf(), FARMING, SkillTable)),
    CARROT(Collection("Carrot", listOf(), FARMING, SkillTable)),
    POTATO(Collection("Potato", listOf(), FARMING, SkillTable)),
    SUGAR_CANE(Collection("Sugar Cane", listOf(), FARMING, SkillTable)),
    CACTUS(Collection("Cactus", listOf(), FARMING, SkillTable)),
    MUSHROOM(Collection("Mushroom", listOf(), FARMING, SkillTable)),
    CHICKEN(Collection("Chicken", listOf(), FARMING, SkillTable)),
    PORK(Collection("Pork", listOf(), FARMING, SkillTable)),
    BEEF(Collection("Beef", listOf(), FARMING, SkillTable)),
    MUTTON(Collection("Mutton", listOf(), FARMING, SkillTable)),
    NETHER_WART(Collection("Nether Wart", listOf(), FARMING, SkillTable)),
    CHORUS(Collection("Chorus", listOf(), FARMING, SkillTable)),

    // fishing
    COD(Collection("Cod", listOf(), FISHING, SkillTable)),
    SALMON(Collection("Salmon", listOf(), FISHING, SkillTable)),
    PUFFERFISH(Collection("Cod", listOf(), FISHING, SkillTable)),
    TROPICAL_FISH(Collection("Cod", listOf(), FISHING, SkillTable)),
    SPONGE(Collection("Cod", listOf(), FISHING, SkillTable)),
    INK_SAC(Collection("Ink Sac", listOf(), FISHING, SkillTable)),
    PRISMARINE(Collection("Prismarine", listOf(), FISHING, SkillTable)),
    SEAWEED(Collection("Seaweed", listOf(), FISHING, SkillTable)),
    MAGMAFISH(Collection("Magmafish", listOf(), FISHING, SkillTable)),

    // foraging
    OAK(Collection("Oak", listOf(), FORAGING, SkillTable)),
    SPRUCE(Collection("Spruce", listOf(), FORAGING, SkillTable)),
    JUNGLE_WOOD(Collection("Jungle Wood", listOf(), FORAGING, SkillTable)),
    DARK_OAK(Collection("Dark Oak", listOf(), FORAGING, SkillTable)),
    BIRCH(Collection("Birch", listOf(), FORAGING, SkillTable)),
    CRIMSON_WOOD(Collection("Crimson Wood", listOf(), FORAGING, SkillTable)),
    WARPED_WOOD(Collection("Warped Wood", listOf(), FORAGING, SkillTable)),
    STEELWOOD(Collection("Steelwood", listOf(), FORAGING, SkillTable)),
    CRYSTALWOOD(Collection("Crystalwood", listOf(), FORAGING, SkillTable)),
    SHADEWOOD(Collection("Shadewood", listOf(), FORAGING, SkillTable)),

    // excavating
    DIRT(Collection("Dirt", listOf(), EXCAVATING, SkillTable)),
    SAND(Collection("Sand", listOf(), EXCAVATING, SkillTable)),
    GRAVEL(Collection("Gravel", listOf(), EXCAVATING, SkillTable)),
    NYLIUM(Collection("Nylium", listOf(), EXCAVATING, SkillTable)),
    NETHERRACK(Collection("Netherrack", listOf(), EXCAVATING, SkillTable)),
    MOONSTONE_DUST(Collection("Moonstone Dust", listOf(), EXCAVATING, SkillTable)),

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
