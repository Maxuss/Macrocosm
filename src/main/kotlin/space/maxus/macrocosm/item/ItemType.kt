package space.maxus.macrocosm.item

@Suppress("UNUSED")
enum class ItemType(
    val weapon: Boolean = true,
    val tool: Boolean = false,
    val armor: Boolean = false,
    val leftHand: Boolean = false,
    val equipment: Boolean = false
) {
    SWORD,
    LONGSWORD,
    DAGGER,
    BOW,
    STAFF,
    AXE(tool = true),
    PICKAXE(false, true),
    DRILL(false, true),
    HOE(false, true),
    SHOVEL(false, true),
    SHIELD(false, leftHand = true),
    WAND(false, leftHand = true),
    GAUNTLET(true, true, leftHand = true),
    FISHING_ROD(false, true),

    HELMET(false, false, true),
    CHESTPLATE(false, false, true),
    LEGGINGS(false, false, true),
    BOOTS(false, false, true),

    ACCESSORY(false),
    CHARM(false),

    ENCHANTED_BOOK(false),

    DEPLOYABLE(false),
    TURRET(false),
    CONSUMABLE(false),
    REFORGE_STONE(false),
    POWER_STONE(false),
    SCROLL(false),
    OTHER(false, leftHand = true),

    NECKLACE(false, equipment = true),
    CLOAK(false, equipment = true),
    BELT(false, equipment = true),
    GLOVES(false, equipment = true)
    ;

    override fun toString(): String {
        return if (this == OTHER) "" else name.replace("_", " ")
    }

    companion object {
        fun melee() = listOf(AXE, SWORD, LONGSWORD, GAUNTLET, DAGGER, STAFF)
        fun ranged() = listOf(BOW)
        fun weapons() = listOf(AXE, LONGSWORD, SWORD, GAUNTLET, BOW, DAGGER, STAFF)
        fun weaponsWand() = listOf(AXE, LONGSWORD, SWORD, GAUNTLET, BOW, WAND, DAGGER, STAFF)
        fun tools() = listOf(PICKAXE, HOE, AXE, SHOVEL, GAUNTLET, DRILL, FISHING_ROD)
        fun mining() = listOf(PICKAXE, GAUNTLET, DRILL)
        fun leftHand() = listOf(SHIELD, WAND, CHARM)
        fun armor() = listOf(HELMET, CHESTPLATE, LEGGINGS, BOOTS, CLOAK)
        fun accessories() = listOf(ACCESSORY, CHARM)
        fun misc() = listOf(DEPLOYABLE, CONSUMABLE, OTHER, REFORGE_STONE)
        fun all() = values().toList()
    }
}
