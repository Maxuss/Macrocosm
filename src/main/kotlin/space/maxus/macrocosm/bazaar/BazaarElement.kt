package space.maxus.macrocosm.bazaar

import org.bukkit.Material
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.withAll

enum class BazaarElement(val item: MacrocosmItem? = null, val vanilla: Boolean = false) {
    // mining
    COBBLESTONE(vanilla = true),
    ENCHANTED_COBBLESTONE,
    DEEPSLATE(vanilla = true),
    ENCHANTED_DEEPSLATE,
    BLACKSTONE(vanilla = true),
    ENCHANTED_BLACKSTONE,

    COAL(vanilla = true),
    ENCHANTED_COAL,
    ENCHANTED_COAL_BLOCK,

    IRON_INGOT(vanilla = true),
    ENCHANTED_IRON_INGOT,
    ENCHANTED_IRON_BLOCK,

    GOLD_INGOT(vanilla = true),
    ENCHANTED_GOLD_INGOT,
    ENCHANTED_GOLD_BLOCK,

    DIAMOND(vanilla = true),
    ENCHANTED_DIAMOND,
    ENCHANTED_DIAMOND_BLOCK,

    LAPIS_LAZULI(vanilla = true),
    ENCHANTED_LAPIS_LAZULI,
    ENCHANTED_LAPIS_BLOCK,

    EMERALD(vanilla = true),
    ENCHANTED_EMERALD,
    ENCHANTED_EMERALD_BLOCK,

    REDSTONE(vanilla = true),
    ENCHANTED_REDSTONE,
    ENCHANTED_REDSTONE_BLOCK,

    QUARTZ(vanilla = true),
    ENCHANTED_QUARTZ,
    ENCHANTED_QUARTZ_BLOCK,

    OBSIDIAN(vanilla = true),
    ENCHANTED_OBSIDIAN,

    GLOWSTONE_DUST(vanilla = true),
    ENCHANTED_GLOWSTONE_DUST,
    ENCHANTED_GLOWSTONE,

    ICE(vanilla = true),
    ENCHANTED_ICE,
    ENCHANTED_PACKED_ICE,

    // excavating
    NETHERRACK(vanilla = true),
    ENCHANTED_NETHERRACK,

    GRAVEL(vanilla = true),
    ENCHANTED_GRAVEL,
    FLINT(vanilla = true),
    ENCHANTED_FLINT,

    SAND(vanilla = true),
    ENCHANTED_SAND,
    RED_SAND(vanilla = true),
    ENCHANTED_RED_SAND,

    SCULK(vanilla = true),
    ENCHANTED_SCULK,

    SOUL_SAND(vanilla = true),
    ENCHANTED_SOUL_SAND,

    SNOWBALL(vanilla = true),
    SNOW_BLOCK(vanilla = true),
    ENCHANTED_SNOWBALL,
    ENCHANTED_SNOW_BLOCK,

    END_STONE(vanilla = true),
    ENCHANTED_END_STONE,

    // combat
    ROTTEN_FLESH(vanilla = true),
    ENCHANTED_ROTTEN_FLESH,

    BONE(vanilla = true),
    ENCHANTED_BONE,
    ENCHANTED_BONE_BLOCK,

    STRING(vanilla = true),
    ENCHANTED_STRING,
    SPIDER_EYE(vanilla = true),
    ENCHANTED_SPIDER_EYE,
    ENCHANTED_FERMENTED_SPIDER_EYE,

    GUNPOWDER(vanilla = true),
    ENCHANTED_GUNPOWDER,

    ENDER_PEARL(vanilla = true),
    ENCHANTED_ENDER_PEARL,
    ENCHANTED_ENDER_EYE,

    GHAST_TEAR(vanilla = true),
    ENCHANTED_GHAST_TEAR,

    PHANTOM_MEMBRANE(vanilla = true),
    ENCHANTED_PHANTOM_MEMBRANE,

    SLIME_BALL(vanilla = true),
    ENCHANTED_SLIME_BALL,
    ENCHANTED_SLIME_BLOCK,
    MAGMA_CREAM(vanilla = true),
    ENCHANTED_MAGMA_CREAM,
    ENCHANTED_MAGMA_BLOCK,

    BLAZE_ROD(vanilla = true),
    ENCHANTED_BLAZE_POWDER,
    ENCHANTED_BLAZE_ROD,

    REVENANT_FLESH,
    FOUL_FLESH,
    RANCID_FLESH,
    REVENANT_VISCERA,
    REVENANT_INNARDS,

    FLAMING_ASHES,
    BLOODY_CINDER,
    BLAZING_ALLOY,

    // foraging
    OAK_LOG(vanilla = true),
    ENCHANTED_OAK_LOG,

    SPRUCE_LOG(vanilla = true),
    ENCHANTED_SPRUCE_LOG,

    BIRCH_LOG(vanilla = true),
    ENCHANTED_BIRCH_LOG,

    DARK_OAK_LOG(vanilla = true),
    ENCHANTED_DARK_OAK_LOG,

    ACACIA_LOG(vanilla = true),
    ENCHANTED_ACACIA_LOG,

    JUNGLE_LOG(vanilla = true),
    ENCHANTED_JUNGLE_LOG,

    // fishing
    COD(vanilla = true),
    ENCHANTED_RAW_COD,
    ENCHANTED_COOKED_COD,

    SALMON(vanilla = true),
    ENCHANTED_SALMON,
    ENCHANTED_COOKED_SALMON,

    TROPICAL_FISH(vanilla = true),
    ENCHANTED_TROPICAL_FISH,
    PUFFERFISH(vanilla = true),
    ENCHANTED_PUFFERFISH,

    KELP(vanilla = true),
    ENCHANTED_KELP,
    ENCHANTED_DRIED_KELP,
    ENCHANTED_DRIED_KELP_BLOCK,
    LILY_PAD(vanilla = true),
    ENCHANTED_LILY_PAD,

    PRISMARINE_SHARD(vanilla = true),
    ENCHANTED_PRISMARINE_SHARD,
    PRISMARINE_CRYSTALS(vanilla = true),
    ENCHANTED_PRISMARINE_CRYSTALS,
    ENCHANTED_PRISMARINE,
    ENCHANTED_DARK_PRISMARINE,

    CLAY_BALL(vanilla = true),
    ENCHANTED_CLAY_BALL,
    ENCHANTED_CLAY,

    INK_SAC(vanilla = true),
    ENCHANTED_INK_SAC,

    SPONGE(vanilla = true),
    ENCHANTED_SPONGE,
    ENCHANTED_WET_SPONGE,

    // farming
    WHEAT(vanilla = true),
    ENCHANTED_WHEAT,
    ENCHANTED_HAY_BLOCK,

    CARROT(vanilla = true),
    ENCHANTED_CARROT,
    ENCHANTED_GOLDEN_CARROT,

    POTATO(vanilla = true),
    ENCHANTED_POTATO,
    ENCHANTED_BAKED_POTATO,

    PUMPKIN(vanilla = true),
    ENCHANTED_PUMPKIN,

    MELON_SLICE(vanilla = true),
    ENCHANTED_MELON_SLICE,
    ENCHANTED_MELON,
    ENCHANTED_GLISTERING_MELON,

    RED_MUSHROOM(vanilla = true),
    BROWN_MUSHROOM(vanilla = true),
    ENCHANTED_RED_MUSHROOM,
    ENCHANTED_BROWN_MUSHROOM,
    ENCHANTED_RED_MUSHROOM_BLOCK,
    ENCHANTED_BROWN_MUSHROOM_BLOCK,

    CHORUS_FRUIT(vanilla = true),
    ENCHANTED_CHORUS_FRUIT,
    ENCHANTED_CHORUS_FLOWER,

    COCOA_BEANS(vanilla = true),
    ENCHANTED_COCOA_BEANS,
    ENCHANTED_COOKIE,

    CACTUS(vanilla = true),
    ENCHANTED_GREEN_DYE,
    ENCHANTED_CACTUS,

    BAMBOO(vanilla = true),
    ENCHANTED_BAMBOO,

    BEETROOT(vanilla = true),
    ENCHANTED_BEETROOT,

    SUGAR_CANE(vanilla = true),
    ENCHANTED_SUGAR,
    ENCHANTED_SUGAR_CANE,

    PORKCHOP(vanilla = true),
    ENCHANTED_PORKCHOP,
    ENCHANTED_COOKED_PORKCHOP,

    BEEF(vanilla = true),
    ENCHANTED_BEEF,
    ENCHANTED_COOKED_BEEF,

    MUTTON(vanilla = true),
    ENCHANTED_MUTTON,
    ENCHANTED_COOKED_MUTTON,

    RABBIT(vanilla = true),
    ENCHANTED_RABBIT,
    ENCHANTED_COOKED_RABBIT,
    ENCHANTED_RABBIT_FOOT,
    ENCHANTED_RABBIT_HIDE,

    CHICKEN(vanilla = true),
    ENCHANTED_CHICKEN,
    ENCHANTED_COOKED_CHICKEN,
    ENCHANTED_EGG,

    HONEYCOMB(vanilla = true),
    ENCHANTED_HONEYCOMB,
    ENCHANTED_HONEY_BLOCK,
    ENCHANTED_HONEYCOMB_BLOCK,

    NETHER_WART(vanilla = true),
    ENCHANTED_NETHER_WART,
    ENCHANTED_NETHER_WART_BLOCK

    // todo: special materials
    ;

    companion object {
        val allKeys by lazy { Registry.BAZAAR_ELEMENTS_REF.iter().keys.withAll(Registry.BAZAAR_ELEMENTS.iter().keys.withAll(Registry.BAZAAR_ELEMENTS_VANILLA.iter().keys)) }

        fun idToCollection(id: Identifier): BazaarCollection? {
            return BazaarCollection.values().firstOrNull { coll -> coll.items.contains(id) }
        }
        fun idToElement(id: Identifier): MacrocosmItem? {
            return Registry.BAZAAR_ELEMENTS.findOrNull(id) ?: Registry.ITEM.findOrNull(Registry.BAZAAR_ELEMENTS_REF.findOrNull(id) ?: return VanillaItem(Material.valueOf(id.path.uppercase())))
        }

        fun init() {
            Threading.runAsyncRaw {
                val pool = Threading.newFixedPool(12)

                for(value in values()) {
                    pool.execute {
                        val id = id(value.name.lowercase())
                        if(!value.vanilla && value.item != null) {
                            Registry.BAZAAR_ELEMENTS.register(id, value.item)
                        } else if(value.vanilla) {
                            Registry.BAZAAR_ELEMENTS_VANILLA.register(Identifier("minecraft", value.name.lowercase()), Material.valueOf(value.name))
                        } else {
                            Registry.BAZAAR_ELEMENTS_REF.register(id, id)
                        }
                    }
                }
                pool.shutdown()
            }
        }
    }
}
