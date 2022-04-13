package space.maxus.macrocosm.db

import java.sql.Statement

interface DatabaseStore {
    fun storeSelf(stmt: Statement)
}
