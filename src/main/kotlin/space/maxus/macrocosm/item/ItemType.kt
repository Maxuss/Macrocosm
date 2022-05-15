package space.maxus.macrocosm.item

@Suppress("UNUSED")
enum class ItemType(
    val weapon: Boolean = true,
    val tool: Boolean = false,
    val armor: Boolean = false,
    val leftHand: Boolean = false
) {
    SWORD,
    BOW,
    AXE(tool = true),
    PICKAXE(false, true),
    DRILL(false, true),
    HOE(false, true),
    SHOVEL(false, true),
    SHIELD(false, leftHand = true),
    WAND(false, leftHand = true),
    GAUNTLET(true, true, leftHand = true),

    HELMET(false, false, true),
    CHESTPLATE(false, false, true),
    LEGGINGS(false, false, true),
    BOOTS(false, false, true),
    CLOAK(false, false, true),

    ACCESSORY(false),
    CHARM(false, leftHand = true),

    ENCHANTED_BOOK(false),

    DEPLOYABLE(false),
    CONSUMABLE(false),
    OTHER(false, leftHand = true),
    REFORGE_STONE(false)

    ;

    override fun toString(): String {
        return if (this == OTHER) "" else name
    }

    companion object {
        fun melee() = listOf(AXE, SWORD, GAUNTLET)
        fun ranged() = listOf(BOW)
        fun weapons() = listOf(AXE, SWORD, GAUNTLET, BOW)
        fun tools() = listOf(PICKAXE, HOE, AXE, SHOVEL, GAUNTLET, DRILL)
        fun leftHand() = listOf(SHIELD, WAND, CHARM)
        fun armor() = listOf(HELMET, CHESTPLATE, LEGGINGS, BOOTS, CLOAK)
        fun accessories() = listOf(ACCESSORY, CHARM)
        fun misc() = listOf(DEPLOYABLE, CONSUMABLE, OTHER, REFORGE_STONE)
        fun all() = values().toList()
    }
}
