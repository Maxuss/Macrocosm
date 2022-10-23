package space.maxus.macrocosm.db.impl.local

import org.jetbrains.annotations.ApiStatus
import org.jetbrains.exposed.sql.Database
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.db.impl.AbstractSQLDatabase

/**
 * A legacy SQL database implementation that stored the data locally inside an SQLite database file
 *
 * NOTE: this database does not work on the latest builds since the exclusion of the SQLite driver library
 */
@ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
@Deprecated(
    "SQLite database was deprecated since the migration to the Postgres database",
    ReplaceWith("PostgresDatabaseImpl"),
    DeprecationLevel.ERROR
)
object SqliteDatabaseImpl : AbstractSQLDatabase() {
    override fun obtainConnection(): Database {
        val players = Accessor.access("macrocosm.db")
        return Database.connect("jdbc:sqlite:$players")
    }
}
