package space.maxus.macrocosm.item.buffs

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

object Buffs {
    fun init() {
        Registry.ITEM_BUFF.register(id("potato_book"), PotatoBook)
    }
}
