package space.maxus.macrocosm.collections

import com.google.gson.reflect.TypeToken
import space.maxus.macrocosm.util.GSON

class Collections(private val colls: HashMap<CollectionType, PlayerCollection>) {

    operator fun get(coll: CollectionType): Int {
        return colls[coll]!!.total
    }

    operator fun set(coll: CollectionType, exp: Int) {
        colls[coll]!!.total = exp
    }

    fun increase(coll: CollectionType, amount: Int): Boolean {
        colls[coll]!!.total = colls[coll]!!.total + amount
        return coll.inst.table.shouldLevelUp(colls[coll]!!.lvl, colls[coll]!!.total.toDouble(), amount.toDouble())
    }

    fun level(coll: CollectionType): Int {
        return colls[coll]!!.lvl
    }

    fun setLevel(coll: CollectionType, lvl: Int) {
        colls[coll]!!.lvl = lvl
    }

    fun json(): String {
        return GSON.toJson(colls.map { (key, value) -> Pair(key.name, value) }.toMap())
    }

    companion object {
        fun default(): Collections =
            Collections(HashMap(CollectionType.values().associateWith { PlayerCollection(0, 0) }))

        fun fromJson(json: String): Collections {
            val map: HashMap<String, PlayerCollection> =
                GSON.fromJson(json, object : TypeToken<HashMap<String, PlayerCollection>>() {}.type)
            return Collections(HashMap(map.map { (key, value) -> Pair(CollectionType.valueOf(key), value) }.toMap()))
        }
    }
}

data class PlayerCollection(var lvl: Int, var total: Int)
