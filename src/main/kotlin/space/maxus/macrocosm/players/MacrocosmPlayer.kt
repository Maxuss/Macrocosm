package space.maxus.macrocosm.players

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.ranks.Rank
import java.sql.Statement
import java.time.Instant
import java.util.*

@Suppress("unused")
class MacrocosmPlayer(val ref: UUID): DatabaseStore {
    val paper: Player? = Bukkit.getServer().getPlayer(ref)

    var rank: Rank = Rank.NONE
    var firstJoin: Long = Instant.now().toEpochMilli()
    var lastJoin: Long = Instant.now().toEpochMilli()
    var playtime: Long = 0

    override fun storeSelf(stmt: Statement) {
        val player = paper
        if(player == null) {
            println("Tried to store offline player $ref")
            return
        }
        val newPlaytime = playtime + (Instant.now().toEpochMilli() - lastJoin)

        stmt.executeUpdate("REPLACE INTO Players VALUES ('$ref', ${rank.id()}, $firstJoin, $lastJoin, $newPlaytime)")
        stmt.close()
    }

    fun playtimeMillis() = playtime + (Instant.now().toEpochMilli() - lastJoin)

    companion object {
        fun readPlayer(id: UUID): MacrocosmPlayer? {
            val stmt = Database.statement
            val res = stmt.executeQuery("SELECT * FROM Players where UUID = '$id'")
            if(!res.next())
                return null
            val rank = Rank.fromId(res.getInt("RANK"))
            val firstJoin = res.getInt("FIRST_JOIN")
            val playtime = res.getInt("PLAYTIME")
            val player = MacrocosmPlayer(id)
            player.rank = rank
            player.firstJoin = firstJoin.toLong()
            player.lastJoin = Instant.now().toEpochMilli()
            player.playtime = playtime.toLong()
            stmt.close()
            return player
        }
    }
}
