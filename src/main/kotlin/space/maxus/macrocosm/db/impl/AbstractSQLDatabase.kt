package space.maxus.macrocosm.db.impl

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import space.maxus.macrocosm.db.DataStorage
import space.maxus.macrocosm.db.LimitedItemsTable
import space.maxus.macrocosm.db.PlayersTable
import space.maxus.macrocosm.db.StatsTable
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic
import java.util.*

abstract class AbstractSQLDatabase: DataStorage {
    private lateinit var connection: Database

    protected abstract fun obtainConnection(): Database

    override fun connect() {
        if(::connection.isInitialized)
            return
        connection = obtainConnection()

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.createMissingTablesAndColumns(PlayersTable, LimitedItemsTable, StatsTable)
        }

        var statQuery = "CREATE TABLE IF NOT EXISTS Stats(UUID VARCHAR PRIMARY KEY"
        for (stat in Statistic.values()) {
            statQuery += ", ${stat.name} REAL"
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

    override fun <T> transact(transact: Transaction.() -> T): T  = transaction(connection, transact)
}
