package space.maxus.macrocosm.players

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.jetbrains.annotations.NotNull
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
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

    var mainHand: MacrocosmItem?
        get() {
            val item = paper?.inventory?.itemInMainHand ?: return null
            if(item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setItemInMainHand(value!!.build()) ?: Unit

    var offHand: MacrocosmItem?
        get() {
            val item = paper?.inventory?.itemInOffHand ?: return null
            if(item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setItemInOffHand(value!!.build()) ?: Unit

    var helmet: MacrocosmItem?
        get() {
            val item = paper?.inventory?.helmet ?: return null
            if(item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setHelmet(value!!.build()) ?: Unit

    var chestplate: MacrocosmItem?
        get() {
            val item = paper?.inventory?.chestplate ?: return null
            if(item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setChestplate(value!!.build()) ?: Unit

    var leggings: MacrocosmItem?
        get() {
            val item = paper?.inventory?.leggings ?: return null
            if(item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setLeggings(value!!.build()) ?: Unit

    var boots: MacrocosmItem?
        get() {
            val item = paper?.inventory?.boots ?: return null
            if(item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setBoots(value!!.build()) ?: Unit

    fun calculateStats(): Statistics? {
        if(paper == null)
            return null

        val cloned = baseStats.clone()
        EquipmentSlot.values().map {
            val baseItem = paper.inventory.getItem(it)
            if(baseItem.type == Material.AIR)
                return@map Statistics.zero()
            val item = ItemRegistry.toMacrocosm(baseItem)
            cloned.increase(item.stats)
        }

        return cloned
    }

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
