package space.maxus.macrocosm.npc.shop

interface Sellable {
    val sellPrice: Number
}

interface Unsellable: Sellable {
    override val sellPrice: Number get() = -1
}
