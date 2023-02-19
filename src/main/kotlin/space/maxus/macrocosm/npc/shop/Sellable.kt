package space.maxus.macrocosm.npc.shop

/**
 * Marks that the item can be sold
 */
interface Sellable {
    val sellPrice: Number
}

/**
 * Marks that the item can **NOT** be sold
 */
interface Unsellable : Sellable {
    override val sellPrice: Number get() = -1
}
