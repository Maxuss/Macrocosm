@file:Suppress("unused")

package space.maxus.macrocosm.reforge

object ReforgeRegistry {
    val reforges: HashMap<String, Reforge> = hashMapOf()

    fun register(name: String, reforge: Reforge): Reforge {
        if(reforges.containsKey(name))
            return reforge
        reforges[name] = reforge
        return reforge
    }

    fun nameOf(ref: Reforge) = reforges.filter { (_, v) -> v == ref }.map { (k, _) -> k }.first()

    fun find(name: String) = reforges[name]
}
