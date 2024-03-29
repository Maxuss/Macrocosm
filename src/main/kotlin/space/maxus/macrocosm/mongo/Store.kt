package space.maxus.macrocosm.mongo

/**
 * An interface used to identify classes that can store themselves into a mongo database
 */
interface Store {
    /**
     * Stores itself into a mongo database
     */
    fun store()
}
