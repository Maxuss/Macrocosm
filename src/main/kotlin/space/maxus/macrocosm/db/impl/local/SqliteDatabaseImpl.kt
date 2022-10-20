package space.maxus.macrocosm.db.impl.local

import org.jetbrains.exposed.sql.Database
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.db.impl.AbstractSQLDatabase

@Deprecated(
    "SQLite database was deprecated since the migration to the Postgres database",
    ReplaceWith("PostgresDatabaseImpl")
)
object SqliteDatabaseImpl : AbstractSQLDatabase() {
    override fun obtainConnection(): Database {
        val players = Accessor.access("macrocosm.db")
        return Database.connect("jdbc:sqlite:$players")
    }
}
