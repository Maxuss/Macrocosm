package space.maxus.macrocosm.db.impl.postgres

import org.jetbrains.exposed.sql.Database
import space.maxus.macrocosm.db.impl.AbstractSQLDatabase
import space.maxus.macrocosm.logger

/**
 * A current database implementation, storing itself inside a Postgres database
 */
class PostgresDatabaseImpl(val address: String) : AbstractSQLDatabase() {
    override fun obtainConnection(): Database {
        try {
            // preloading postgres driver
            Class.forName("org.postgresql.Driver")
        } catch (e: ClassNotFoundException) {
            logger.severe("Could not find Postgres driver!")
            error("Tried to launch postgres database without postgres driver!")
        }
        return Database.connect(
            "jdbc:postgresql://$address/macrocosm",
            "org.postgresql.Driver",
            user = System.getProperty("macrocosm.postgres.user"),
            password = System.getProperty("macrocosm.postgres.pass")
        )
    }
}
