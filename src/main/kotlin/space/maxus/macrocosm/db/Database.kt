package space.maxus.macrocosm.db

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
    private var firstStart: Boolean = false

    val statement: Statement get() = playerDb.createStatement()

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
                """CREATE TABLE Players(
                UUID VARCHAR PRIMARY KEY,
                RANK INT,
                FIRST_JOIN INT,
                LAST_JOIN INT,
                PLAYTIME INT)
                """.trimIndent()
            )
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
}
