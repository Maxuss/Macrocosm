package space.maxus.macrocosm.db

import org.jetbrains.exposed.sql.Transaction
import space.maxus.macrocosm.registry.Identifier
import java.util.*

/**
 * A high-level interface over databases, implement those to create your own databases
 */
interface DataStorage {
    /**
     * Connects to a database
     */
    fun connect()

    /**
     * Reads all players from a database
     */
    fun readPlayers(): List<UUID>

    /**
     * Increments count of limited edition item
     */
    fun incrementLimitedEdition(itemId: Identifier): Int

    /**
     * Performs an Exposed transaction over this database
     */
    fun <T> transact(transact: Transaction.() -> T): T
}
