package space.maxus.macrocosm.bazaar

import net.axay.kspigot.items.flags
import net.axay.kspigot.items.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.unwrapInner

enum class BazaarCollection(val displayName: String, val displayItem: ItemStack, vararg containing: String) {
    // mining
    COBBLESTONE("<aqua>Cobblestone", ItemStack(Material.COBBLESTONE), "cobblestone", "deepslate", "blackstone"),
    COAL("<yellow>Coal", ItemStack(Material.COAL), "coal"),
    IRON("<aqua>Iron", ItemStack(Material.IRON_INGOT), "iron_ingot"),
    GOLD("<gold>Gold", ItemStack(Material.GOLD_INGOT), "gold_ingot"),
    DIAMOND("<aqua>Diamond", ItemStack(Material.DIAMOND), "diamond"),
    LAPIS("<aqua>Lapis", ItemStack(Material.LAPIS_LAZULI), "lapis_lazuli"),
    EMERALD("<green>Emerald", ItemStack(Material.EMERALD), "emerald"),
    REDSTONE("<red>Redstone", ItemStack(Material.REDSTONE), "redstone"),
    QUARTZ("<yellow>Quartz", ItemStack(Material.QUARTZ), "quartz"),
    OBSIDIAN("<aqua>Obsidian", ItemStack(Material.OBSIDIAN), "obsidian"),
    GLOWSTONE("<gold>Glowstone", ItemStack(Material.GLOWSTONE_DUST), "glowstone"),
    ICE("<aqua>Ice", ItemStack(Material.ICE), "(?<!sl)ice"),

    // excavating
    NETHERRACK("<gold>Netherrack", ItemStack(Material.NETHERRACK), "netherrack"),
    GRAVEL("<aqua>Gravel", ItemStack(Material.GRAVEL), "gravel", "flint"),
    SAND("<yellow>Sand", ItemStack(Material.SAND), "(?<!soul_)sand"),
    SOUL_SAND("<gold>Soul Sand", ItemStack(Material.SOUL_SAND), "soul_sand"),
    SNOW("<aqua>Snow", ItemStack(Material.SNOWBALL), "snow"),
    END_STONE("<yellow>End Stone", ItemStack(Material.END_STONE), "end_stone"),
    SCULK("<light_purple>Sculk", ItemStack(Material.SCULK), "sculk"),

    // combat
    ROTTEN_FLESH("<green>Rotten Flesh", ItemStack(Material.ROTTEN_FLESH), "rotten_flesh"),
    BONE("<aqua>Bone", ItemStack(Material.BONE), "bone"),
    SPIDERS("<yellow>Spiders", ItemStack(Material.STRING), "string", "spider_eye"),
    GUNPOWDER("<green>Gunpowder", ItemStack(Material.GUNPOWDER), "gunpowder"),
    ENDER_PEARL("<aqua>Ender Pearl", ItemStack(Material.ENDER_PEARL), "ender_pearl", "ender_eye"),
    GHAST_TEAR("<yellow>Ghast Tear", ItemStack(Material.GHAST_TEAR), "ghast_tear"),
    PHANTOM_MEMBRANE("<light_purple>Phantom Membrane", ItemStack(Material.PHANTOM_MEMBRANE), "phantom_membrane"),
    SLIMES("<green>Slimes", ItemStack(Material.SLIME_BALL), "slime_ball", "slime_block", "magma_cream", "magma_block"),
    BLAZE_ROD("<gold>Blaze Rod", ItemStack(Material.BLAZE_ROD), "blaze_rod", "blaze_powder"),
    REVENANT_HORROR("<red>Revenant Horror", ItemStack(Material.ROTTEN_FLESH).apply { meta { addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1, true); flags(*org.bukkit.inventory.ItemFlag.values()) } }, "revenant_flesh", "foul_flesh", "rancid_flesh", "revenant_viscera", "revenant_innards"),
    CINDERFLAME_SPIRIT("<gold>Cinderflame Spirit", ItemStack(Material.CHARCOAL).apply { meta { addEnchant(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1, true); flags(*org.bukkit.inventory.ItemFlag.values()) } }, "flaming_ashes", "bloody_cinder", "blazing_alloy"),

    // foraging
    OAK("<yellow>Oak", ItemStack(Material.OAK_LOG), "(?<!dark_)oak"),
    SPRUCE("<green>Spruce", ItemStack(Material.SPRUCE_LOG), "spruce"),
    BIRCH("<aqua>Birch", ItemStack(Material.BIRCH_LOG), "birch"),
    DARK_OAK("<dark_green>Dark Oak", ItemStack(Material.DARK_OAK_LOG), "dark_oak"),
    ACACIA("<gold>Acacia", ItemStack(Material.ACACIA_LOG), "acacia"),
    JUNGLE("<green>Jungle", ItemStack(Material.JUNGLE_LOG), "jungle"),

    // fishing
    COD("<yellow>Cod", ItemStack(Material.COD), "cod"),
    SALMON("<red>Salmon", ItemStack(Material.SALMON), "salmon"),
    TROPICAL_FISH("<gold>Tropical Fish", ItemStack(Material.TROPICAL_FISH), "tropical_fish", "pufferfish"),
    SEA_VEGETATION("<green>Sea Vegetation", ItemStack(Material.KELP), "kelp", "lily_pad"),
    PRISMARINE("<aqua>Prismarine", ItemStack(Material.PRISMARINE_CRYSTALS), "prismarine"),
    CLAY("<aqua>Clay", ItemStack(Material.CLAY_BALL), "clay"),
    INK("<blue>Ink", ItemStack(Material.INK_SAC), "ink_sac"),
    SPONGE("<yellow>Sponge", ItemStack(Material.SPONGE), "sponge"),

    // farming
    WHEAT("<yellow>Wheat", ItemStack(Material.WHEAT), "wheat", "hay"),
    CARROT("<gold>Carrot", ItemStack(Material.CARROT), "carrot"),
    POTATO("<gold>Potato", ItemStack(Material.POTATO), "potato"),
    PUMPKIN("<gold>Pumpkin", ItemStack(Material.PUMPKIN), "pumpkin"),
    MELON("<red>Melon", ItemStack(Material.MELON_SLICE), "melon"),
    MUSHROOM("<aqua>Mushrooms", ItemStack(Material.RED_MUSHROOM), "mushroom"),
    COCOA("<gold>Cocoa", ItemStack(Material.COCOA_BEANS), "cocoa", "cookie"),
    CACTUS("<dark_green>Cactus", ItemStack(Material.CACTUS), "cactus"),
    BAMBOO("<green>Bamboo", ItemStack(Material.BAMBOO), "bamboo"),
    BEETROOT("<red>Beetroot", ItemStack(Material.BEETROOT), "beetroot"),
    SUGAR_CANE("<green>Sugar Cane", ItemStack(Material.SUGAR_CANE), "sugar"),
    NETHER_WART("<red>Nether Wart", ItemStack(Material.NETHER_WART), "nether_wart"),
    CHORUS("<light_purple>Chorus", ItemStack(Material.CHORUS_FRUIT), "chorus"),
    PORKCHOP("<red>Porkchop", ItemStack(Material.PORKCHOP), "porkchop"),
    BEEF("<red>Beef", ItemStack(Material.BEEF), "beef"),
    MUTTON("<red>Mutton", ItemStack(Material.MUTTON), "mutton"),
    RABBIT("<red>Rabbit", ItemStack(Material.RABBIT), "rabbit"),
    CHICKEN("<red>Chicken", ItemStack(Material.CHICKEN), "chicken", "egg"),
    HONEY("<gold>Honey", ItemStack(Material.HONEYCOMB), "honey")
    ;

    val items by lazy {
        containing.map { part -> val u = part.uppercase(); BazaarElement.values().filter { it.name.contains(u.toRegex()) } }.unwrapInner().map { ele -> if(ele.vanilla) id("minecraft", ele.name.lowercase()) else id(ele.name.lowercase()) }
    }
}
