package space.maxus.macrocosm.db

import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.notExists

object Database {
    private lateinit var playerDb: Connection
    private lateinit var bazaarDb: Connection
    private var firstStart: Boolean = false

    val statement: Statement get() = playerDb.createStatement()
    val bazaar: Statement get() = bazaarDb.createStatement()

    fun connect() {
        val path = Path.of(System.getProperty("user.dir"), "macrocosm")
        if (path.notExists()) {
            path.createDirectories()
        }
        val players = path.resolve("players.db")
        val lockfile = path.resolve(".lockfile")
        firstStart = lockfile.exists()
        playerDb = DriverManager.getConnection("jdbc:sqlite:$players")
        val st = playerDb.createStatement()
        st.queryTimeout = 30
        if (!firstStart) {
            st.executeUpdate(
                """
                CREATE TABLE Players(
                UUID VARCHAR PRIMARY KEY,
                RANK INT,
                FIRST_JOIN INT,
                LAST_JOIN INT,
                PLAYTIME INT,
                PURSE REAL,
                BANK REAL,
                MEMORY VARCHAR)
                """.trimIndent()
            )
            st.executeUpdate(
                """
                CREATE TABLE SkillsCollections(
                UUID VARCHAR PRIMARY KEY,
                COLLECTIONS VARCHAR DEFAULT('{}'),
                SKILLS VARCHAR DEFAULT('{}')
                )
                """.trimIndent()
            )
            st.executeUpdate(
                """
                CREATE TABLE Recipes(
                UUID VARCHAR PRIMARY KEY,
                RECIPES VARCHAR DEFAULT('[]')
                )
                """.trimIndent()
            )
            st.executeUpdate(
                """
                CREATE TABLE Pets(
                UUID VARCHAR PRIMARY KEY,
                ACTIVE_PET VARCHAR,
                PETS VARCHAR DEFAULT('[]')
                )
                """.trimIndent()
            )
            st.executeUpdate(
                """
                CREATE TABLE LimitedEdition(
                ITEM VARCHAR PRIMARY KEY,
                AMOUNT INT
                )
                """.trimIndent()
            )
            st.executeUpdate(
                """
                    CREATE TABLE Equipment(
                    UUID VARCHAR PRIMARY KEY,
                    NECKLACE VARCHAR,
                    CLOAK VARCHAR,
                    BELT VARCHAR,
                    GLOVES VARCHAR
                    )
                """.trimIndent()
            )

            st.executeUpdate("CREATE TABLE Slayers(UUID VARCHAR PRIMARY KEY, EXPERIENCE VARCHAR DEFAULT('{}'))")

            var statQuery = "CREATE TABLE Stats(UUID VARCHAR PRIMARY KEY"
            for (stat in Statistic.values()) {
                statQuery += ", ${stat.name} REAL"
            }

            st.executeUpdate("$statQuery)")
            lockfile.createFile()
        }
        st.close()
    }

    fun readAllPlayers(): List<UUID> {
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

    object Limited {
        fun incrementGet(item: Identifier): Int {
            val stmt = statement
            val res = stmt.executeQuery("SELECT * FROM LimitedEdition WHERE ITEM='$item'")
            val edition = (if (res.next()) res.getInt("AMOUNT") else 0) + 1
            stmt.executeUpdate("INSERT OR REPLACE INTO LimitedEdition VALUES('$item', $edition)")
            stmt.close()
            return edition
        }
    }
}
