package space.maxus.macrocosm.players

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.stats.Statistics
import java.sql.Statement
import java.time.Instant
import java.util.*

val Player.macrocosm get() = Macrocosm.onlinePlayers[uniqueId]

@Suppress("unused")
class MacrocosmPlayer(val ref: UUID) : DatabaseStore {
    val paper: Player? = Bukkit.getServer().getPlayer(ref)

    var rank: Rank = Rank.NONE
    var firstJoin: Long = Instant.now().toEpochMilli()
    var lastJoin: Long = Instant.now().toEpochMilli()
    var playtime: Long = 0
    var baseStats: Statistics = Statistics.default()
    var purse: Float = 0f
    var bank: Float = 0f

    override fun storeSelf(stmt: Statement) {
        val player = paper
        if (player == null) {
            println("Tried to store offline player $ref")
            return
        }
        val newPlaytime = playtime + (Instant.now().toEpochMilli() - lastJoin)

        stmt.executeUpdate("INSERT OR REPLACE INTO Players VALUES ('$ref', ${rank.id()}, $firstJoin, $lastJoin, $newPlaytime, $purse, $bank)")
        var leftHand = "INSERT OR REPLACE INTO Stats(UUID"
        var rightHand = "VALUES ('$ref'"
        for ((k, value) in baseStats.iter()) {
            leftHand += ", ${k.name}"
            rightHand += ", $value"
        }
        stmt.executeUpdate("$leftHand)$rightHand)")
        stmt.close()
    }

    fun playtimeMillis() = playtime + (Instant.now().toEpochMilli() - lastJoin)

    companion object {
        fun readPlayer(id: UUID): MacrocosmPlayer? {
            val stmt = Database.statement
            val res = stmt.executeQuery("SELECT * FROM Players where UUID = '$id'")
            if (!res.next())
                return null
            val rank = Rank.fromId(res.getInt("RANK"))
            val firstJoin = res.getLong("FIRST_JOIN")
            val playtime = res.getLong("PLAYTIME")
            val purse = res.getFloat("PURSE")
            val bank = res.getFloat("BANK")
            val player = MacrocosmPlayer(id)
            player.rank = rank
            player.firstJoin = firstJoin
            player.lastJoin = Instant.now().toEpochMilli()
            player.playtime = playtime
            player.purse = purse
            player.bank = bank

            val stats = stmt.executeQuery("SELECT * FROM Stats WHERE UUID = '$id'")
            if (!stats.next())
                return null
            player.baseStats = Statistics.fromRes(stats)
            stmt.close()
            return player
        }
    }
}
