package space.maxus.macrocosm.bazaar

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class BazaarElement(val item: MacrocosmItem? = null) {

    ;

    companion object {
        fun init() {
            Threading.runAsyncRaw {
                val pool = Threading.newFixedPool(12)

                for(value in values()) {
                    pool.execute {
                        val id = id(value.name.lowercase())
                        if (value.item == null) {
                            Registry.BAZAAR_ELEMENTS_REF.register(id, id)
                        } else {
                            Registry.BAZAAR_ELEMENTS.register(id, value.item)
                        }
                    }
                }
                pool.shutdown()
            }
        }
    }
}
