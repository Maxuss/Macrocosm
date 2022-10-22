package space.maxus.macrocosm.db.impl

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import space.maxus.macrocosm.db.*
import space.maxus.macrocosm.registry.Identifier
import java.util.*

/**
 * An abstract class over SQL databases
 */
abstract class AbstractSQLDatabase : DataStorage {
    /**
     * An actual connection to a database
     */
    lateinit var connection: Database

    /**
     * Obtains a connection to database
     */
    protected abstract fun obtainConnection(): Database

    final override fun connect() {
        if (::connection.isInitialized)
            return
        connection = obtainConnection()

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(PlayersTable, LimitedItemsTable, StatsTable, BazaarDataTable)
        }
    }

    override fun readPlayers(): List<UUID> = transaction(connection) {
        return@transaction PlayersTable.slice(PlayersTable.uuid).selectAll().map { it[PlayersTable.uuid] }
    }

    override fun incrementLimitedEdition(itemId: Identifier): Int {
        var amount: Int = -1
        transaction {
            LimitedItemsTable.update({ LimitedItemsTable.item eq itemId.toString() }) {
                amount = select { item eq itemId.toString() }.firstOrNull()?.getOrNull(amountObtained) ?: 1
                it[amountObtained] = amount
            }
        }
        return amount
    }

    override fun <T> transact(transact: Transaction.() -> T): T = transaction(connection, transact)
}
