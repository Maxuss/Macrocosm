package space.maxus.macrocosm.db

import org.jetbrains.exposed.sql.Transaction
import space.maxus.macrocosm.registry.Identifier
import java.util.*

interface DataStorage {
    fun connect()
    fun readPlayers(): List<UUID>
    fun incrementLimitedEdition(itemId: Identifier): Int
    fun <T> transact(transact: Transaction.() -> T): T
}
