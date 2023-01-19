package space.maxus.macrocosm.db

/**
 * An interface used to identify classes that can store themselves into a mongo database
 */
interface DatabaseStore {
    /**
     * Stores itself into a mongo database
     */
    fun store()
}
