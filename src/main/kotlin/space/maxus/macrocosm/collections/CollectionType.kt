package space.maxus.macrocosm.collections

import org.bukkit.Material
import space.maxus.macrocosm.collections.Section.*
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.math.SkillTable

/**
 * A type of collection, which is associated with items that are collected for this collection
 */
// todo: collection recipes
// todo: implement an actual collection leveling table instead of a SkillTable
enum class CollectionType(val inst: Collection) {
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

    // excavating
    DIRT(Collection("Dirt", listOf(), EXCAVATING, SkillTable)),
    SAND(Collection("Sand", listOf(), EXCAVATING, SkillTable)),
    GRAVEL(Collection("Gravel", listOf(), EXCAVATING, SkillTable)),
    NYLIUM(Collection("Nylium", listOf(), EXCAVATING, SkillTable)),
    NETHERRACK(Collection("Netherrack", listOf(), EXCAVATING, SkillTable)),
    MOONSTONE_DUST(Collection("Moonstone Dust", listOf(), EXCAVATING, SkillTable)),

    ;

    companion object {
        /**
         * Converts the provided material to a collection type
         *
         * NOTE: In most use cases [CollectionType.fromIdentifier] should be called first because it calls this method under the hood anyway
         * but also covers the null-cases from this method
         *
         * @return null when no collection types match the material
         */
        fun from(mat: Material): CollectionType? {
            return when (mat) {
                Material.ROTTEN_FLESH -> ROTTEN_FLESH
                Material.BONE -> BONE
                Material.SLIME_BALL, Material.SLIME_BLOCK -> SLIME_BALL
                Material.STRING -> STRING
                Material.GUNPOWDER -> GUNPOWDER
                Material.MAGMA_CREAM -> MAGMA_CREAM
                Material.BLAZE_ROD -> BLAZE_ROD
                Material.ENDER_PEARL -> ENDER_PEARL
                Material.GHAST_TEAR -> GHAST_TEAR

                Material.COBBLESTONE, Material.STONE -> COBBLESTONE
                Material.COAL -> COAL
                Material.COPPER_INGOT -> COPPER_INGOT
                Material.REDSTONE -> REDSTONE
                Material.GOLD_INGOT -> GOLD_INGOT
                Material.IRON_INGOT -> IRON_INGOT
                Material.DIAMOND -> DIAMOND
                Material.EMERALD -> EMERALD
                Material.ICE, Material.BLUE_ICE, Material.PACKED_ICE -> ICE
                Material.END_STONE -> END_STONE

                Material.WHEAT -> WHEAT
                Material.CARROT -> CARROT
                Material.POTATO -> POTATO
                Material.SUGAR_CANE -> SUGAR_CANE
                Material.CACTUS -> CACTUS
                Material.MUSHROOM_STEM, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM -> MUSHROOM
                Material.CHICKEN -> CHICKEN
                Material.PORKCHOP -> PORK
                Material.BEEF -> BEEF
                Material.MUTTON -> MUTTON
                Material.NETHER_WART, Material.NETHER_WART_BLOCK -> NETHER_WART
                Material.CHORUS_FRUIT -> CHORUS

                Material.COD -> COD
                Material.SALMON -> SALMON
                Material.PUFFERFISH -> PUFFERFISH
                Material.TROPICAL_FISH -> TROPICAL_FISH
                Material.SPONGE, Material.WET_SPONGE -> SPONGE
                Material.INK_SAC -> INK_SAC
                Material.PRISMARINE_CRYSTALS, Material.PRISMARINE_SHARD -> PRISMARINE
                Material.KELP -> SEAWEED

                Material.OAK_LOG -> OAK
                Material.SPRUCE_LOG -> SPRUCE
                Material.BIRCH_LOG -> BIRCH
                Material.DARK_OAK_LOG -> DARK_OAK
                Material.CRIMSON_STEM -> CRIMSON_WOOD
                Material.WARPED_STEM -> WARPED_WOOD

                Material.DIRT -> DIRT
                Material.SAND -> SAND
                Material.GRAVEL -> GRAVEL
                Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM -> NYLIUM
                Material.NETHERRACK -> NETHERRACK
                else -> null
            }
        }

        /**
         * Converts the provided identifier to a collection type
         *
         * NOTE: this should not be called together with [CollectionType.from] as it is called internally in this method
         */
        fun fromIdentifier(id: Identifier): CollectionType? {
            if (id.path.contains("enchanted_"))
                return from(Material.valueOf(id.path.replace("enchanted_", "").uppercase()))
            return when (id.path) {
                "magmafish" -> MAGMAFISH
                "moonstone_dust" -> MOONSTONE_DUST
                "steelwood" -> STEELWOOD
                "crystalwood" -> CRYSTALWOOD
                "silver" -> SILVER
                "mithril" -> MITHRIL
                "adamantite" -> ADAMANTITE
                "titanium" -> TITANIUM
                else -> null
            }
        }
    }
}
