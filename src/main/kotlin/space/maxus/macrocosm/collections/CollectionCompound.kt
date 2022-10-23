package space.maxus.macrocosm.collections

import space.maxus.macrocosm.serde.Bytes
import space.maxus.macrocosm.serde.Deserialization
import space.maxus.macrocosm.util.general.defer
import java.io.Serializable

/**
 * A compound which contains all collections of a player
 */
class CollectionCompound(val colls: HashMap<CollectionType, PlayerCollection>): Serializable {

    /**
     * Gets total amount of items collected for a certain collection type
     */
    operator fun get(coll: CollectionType): Int {
        return colls[coll]!!.total
    }

    /**
     * Sets total amount of items collected for a certain collection type
     */
    operator fun set(coll: CollectionType, total: Int) {
        colls[coll]!!.total = total
    }

    /**
     * Increases total amount of items collected for a certain collection type by [amount]
     */
    fun increase(coll: CollectionType, amount: Int): Boolean {
        colls[coll]!!.total = colls[coll]!!.total + amount
        return coll.inst.table.shouldLevelUp(colls[coll]!!.lvl, colls[coll]!!.total.toDouble(), amount.toDouble())
    }

    /**
     * Gets collection level for a certain collection type
     */
    fun level(coll: CollectionType): Int {
        return colls[coll]!!.lvl
    }

    /**
     * Sets collection level for a certain collection type
     */
    fun setLevel(coll: CollectionType, lvl: Int) {
        colls[coll]!!.lvl = lvl
    }

    /**
     * Converts this collection to a json string to be stored inside a database
     */
    fun serialize(): String {
        return Bytes.serialize().obj(this).end()
    }

    companion object {
        /**
         * Constructs a default and empty collection compound with all keys from [CollectionType]
         */
        fun default(): CollectionCompound =
            CollectionCompound(HashMap(CollectionType.values().associateWith { PlayerCollection(0, 0) }))

        /**
         * Converts this collection from a json string
         */
        fun deserialize(data: String): CollectionCompound {
            return Bytes.deserialize(data).defer(Deserialization::end).first { obj() }
        }
    }
}

/**
 * An object that stores player's progress for a certain collection
 */
data class PlayerCollection(var lvl: Int, var total: Int): Serializable
