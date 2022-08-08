package space.maxus.macrocosm.db

import space.maxus.macrocosm.registry.Identifier
import java.sql.Statement
import java.util.*

interface DatabaseAccess {
    val statement: Statement

    fun connect()
    fun readPlayers(): List<UUID>
    fun incrementLimitedEdition(item: Identifier): Int
}
