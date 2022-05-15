package space.maxus.macrocosm.item.buffs

import space.maxus.macrocosm.util.id

object Buffs {
    fun init() {
        BuffRegistry.registerBuff(id("potato_book"), PotatoBook)
    }
}
