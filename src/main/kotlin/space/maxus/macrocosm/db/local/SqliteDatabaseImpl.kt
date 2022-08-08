package space.maxus.macrocosm.db.local

import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.db.DatabaseAccess
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.util.*

object SqliteDatabaseImpl: DatabaseAccess {
    private lateinit var playerDb: Connection
    private var firstStart: Boolean = false

    override val statement: Statement get() = playerDb.createStatement()

    override fun connect() {
        val players = Accessor.access("players.db")
        playerDb = DriverManager.getConnection("jdbc:sqlite:$players")
        val st = playerDb.createStatement()
        st.queryTimeout = 30
        st.executeUpdate(
            """
            
            CREATE TABLE IF NOT EXISTS Players(
                UUID VARCHAR UNIQUE PRIMARY KEY,
                RANK INT,
                FIRST_JOIN INT,
                LAST_JOIN INT,
                PLAYTIME INT,
                PURSE REAL,
                BANK REAL,
                MEMORY VARCHAR,
                FORGE VARCHAR
            );
            
            CREATE TABLE IF NOT EXISTS SkillsCollections(
                UUID VARCHAR PRIMARY KEY,
                COLLECTIONS VARCHAR DEFAULT('{}'),
                SKILLS VARCHAR DEFAULT('{}')
            );
            
            CREATE TABLE IF NOT EXISTS Recipes(
                UUID VARCHAR PRIMARY KEY,
                RECIPES VARCHAR DEFAULT('[]')
            );
            
            CREATE TABLE IF NOT EXISTS LimitedTable(
                ITEM VARCHAR PRIMARY KEY,
                AMOUNT INT
            );
            
            CREATE TABLE IF NOT EXISTS Equipment(
                UUID VARCHAR PRIMARY KEY,
                NECKLACE VARCHAR,
                CLOAK VARCHAR,
                BELT VARCHAR,
                GLOVES VARCHAR
            );
            
            CREATE TABLE IF NOT EXISTS Slayers(
                UUID VARCHAR PRIMARY KEY,
                EXPERIENCE VARCHAR DEFAULT('{}')
            );
            
            """.trimIndent()
        )

        var statQuery = "CREATE TABLE IF NOT EXISTS Stats(UUID VARCHAR PRIMARY KEY"
        for (stat in Statistic.values()) {
            statQuery += ", ${stat.name} REAL"
        }

        st.executeUpdate("$statQuery)")

        st.close()
    }

    override fun readPlayers(): List<UUID> {
        val stmt = statement
        val res = stmt.executeQuery("SELECT * FROM Players")
        val out = mutableListOf<UUID>()
        while (res.next()) {
            val id = UUID.fromString(res.getString("UUID"))
            out.add(id)
        }
        stmt.close()
        return out
    }

    override fun incrementLimitedEdition(item: Identifier): Int {
        val stmt = statement
        val res = stmt.executeQuery("SELECT * FROM LimitedEdition WHERE ITEM='$item'")
        val edition = (if (res.next()) res.getInt("AMOUNT") else 0) + 1
        stmt.executeUpdate("INSERT OR REPLACE INTO LimitedEdition VALUES('$item', $edition)")
        stmt.close()
        return edition
    }

}
