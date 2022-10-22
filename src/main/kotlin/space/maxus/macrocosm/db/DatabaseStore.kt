package space.maxus.macrocosm.db

/**
 * An interface used to identify classes that can store themselves into a [DataStorage]
 */
interface DatabaseStore {
    /**
     * Stores itself into a database
     */
    fun storeSelf(data: DataStorage)
}
