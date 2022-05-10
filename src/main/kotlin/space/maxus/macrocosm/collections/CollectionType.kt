package space.maxus.macrocosm.collections

import org.bukkit.Material
import space.maxus.macrocosm.collections.Section.*
import space.maxus.macrocosm.util.Identifier

// todo: collection recipes
enum class CollectionType(val inst: Collection) {
    // combat
    ROTTEN_FLESH(collection("Rotten Flesh", COMBAT, listOf())),
    BONE(collection("Bone", COMBAT, listOf())),
    SLIME_BALL(collection("Slime Ball", COMBAT, listOf())),
    STRING(collection("String", COMBAT, listOf())),
    GUNPOWDER(collection("Gunpowder", COMBAT, listOf())),
    MAGMA_CREAM(collection("Magma Cream", COMBAT, listOf())),
    BLAZE_ROD(collection("Blaze Rod", COMBAT, listOf())),
    ENDER_PEARL(collection("Ender Pearl", COMBAT, listOf())),
    GHAST_TEAR(collection("Ghast Tear", COMBAT, listOf())),

    // mining
    COBBLESTONE(collection("Cobblestone", MINING, listOf())),
    COAL(collection("Coal", MINING, listOf())),
    COPPER_INGOT(collection("Copper Ingot", MINING, listOf())),
    REDSTONE(collection("Redstone", MINING, listOf())),
    GOLD_INGOT(collection("Gold Ingot", MINING, listOf())),
    IRON_INGOT(collection("Iron Ingot", MINING, listOf())),
    DIAMOND(collection("Diamond", MINING, listOf())),
    GEMSTONE(collection("Gemstone", MINING, listOf())),
    EMERALD(collection("Emerald", MINING, listOf())),
    ICE(collection("Ice", MINING, listOf())),
    END_STONE(collection("Endstone", MINING, listOf())),
    SILVER(collection("Silver", MINING, listOf())),
    MITHRIL(collection("Mithril", MINING, listOf())),
    ADAMANTITE(collection("Adamantite", MINING, listOf())),
    TITANIUM(collection("Titanium", MINING, listOf())),

    // farming
    WHEAT(collection("Wheat", FARMING, listOf())),
    CARROT(collection("Carrot", FARMING, listOf())),
    POTATO(collection("Potato", FARMING, listOf())),
    SUGAR_CANE(collection("Sugar Cane", FARMING, listOf())),
    CACTUS(collection("Cactus", FARMING, listOf())),
    MUSHROOM(collection("Mushroom", FARMING, listOf())),
    CHICKEN(collection("Chicken", FARMING, listOf())),
    PORK(collection("Pork", FARMING, listOf())),
    BEEF(collection("Beef", FARMING, listOf())),
    MUTTON(collection("Mutton", FARMING, listOf())),
    NETHER_WART(collection("Nether Wart", FARMING, listOf())),
    CHORUS(collection("Chorus", FARMING, listOf())),

    // fishing
    COD(collection("Cod", FISHING, listOf())),
    SALMON(collection("Salmon", FISHING, listOf())),
    PUFFERFISH(collection("Cod", FISHING, listOf())),
    TROPICAL_FISH(collection("Cod", FISHING, listOf())),
    SPONGE(collection("Cod", FISHING, listOf())),
    INK_SAC(collection("Ink Sac", FISHING, listOf())),
    PRISMARINE(collection("Prismarine", FISHING, listOf())),
    SEAWEED(collection("Seaweed", FISHING, listOf())),
    MAGMAFISH(collection("Magmafish", FISHING, listOf())),

    // foraging
    OAK(collection("Oak", FORAGING, listOf())),
    SPRUCE(collection("Spruce", FORAGING, listOf())),
    JUNGLE_WOOD(collection("Jungle Wood", FORAGING, listOf())),
    DARK_OAK(collection("Dark Oak", FORAGING, listOf())),
    BIRCH(collection("Birch", FORAGING, listOf())),
    CRIMSON_WOOD(collection("Crimson Wood", FORAGING, listOf())),
    WARPED_WOOD(collection("Warped Wood", FORAGING, listOf())),
    STEELWOOD(collection("Steelwood", FORAGING, listOf())),
    CRYSTALWOOD(collection("Crystalwood", FORAGING, listOf())),

    // excavating
    DIRT(collection("Dirt", EXCAVATING, listOf())),
    SAND(collection("Sand", EXCAVATING, listOf())),
    GRAVEL(collection("Gravel", EXCAVATING, listOf())),
    NYLIUM(collection("Nylium", EXCAVATING, listOf())),
    NETHERRACK(collection("Netherrack", EXCAVATING, listOf())),
    MOONSTONE_DUST(collection("Moonstone Dust", EXCAVATING, listOf())),

    ;

    companion object {
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

        fun from(id: Identifier): CollectionType? {
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
