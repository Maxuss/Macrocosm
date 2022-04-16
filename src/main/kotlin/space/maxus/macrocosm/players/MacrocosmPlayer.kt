package space.maxus.macrocosm.players

import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.jetbrains.annotations.NotNull
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.damage.clamp
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import java.sql.Statement
import java.time.Instant
import java.util.*
import kotlin.math.roundToInt

val Player.macrocosm get() = Macrocosm.onlinePlayers[uniqueId]

@Suppress("unused")
class MacrocosmPlayer(val ref: UUID) : DatabaseStore {
    val paper: Player? get() = Bukkit.getServer().getPlayer(ref)

    var rank: Rank = Rank.NONE
    var firstJoin: Long = Instant.now().toEpochMilli()
    var lastJoin: Long = Instant.now().toEpochMilli()
    var playtime: Long = 0
    var baseStats: Statistics = Statistics.default()
    var purse: Float = 0f
    var bank: Float = 0f
    var currentHealth: Float = calculateStats()!!.health
    var currentMana: Float = calculateStats()!!.intelligence
    var lastAbilityUse: HashMap<String, Long> = hashMapOf()

    init {
        // mana & health regen
        task(period = 20L) {
            if (paper == null) {
                it.cancel()
                return@task
            }
            val stats = calculateStats()!!
            if (currentMana < stats.intelligence)
                currentMana += stats.intelligence / 12f
            if (currentHealth < stats.health) {
                currentHealth += stats.health / 20f
                paper!!.health = clamp((currentHealth / stats.health) * 20f, 0f, 20f).toDouble()
            }
        }

        // fixing speed
        task(period = 20L) {
            if (paper == null) {
                it.cancel()
                return@task
            }
            val stats = calculateStats()!!
            paper?.walkSpeed = 0.2F * (stats.speed / 100f)
        }

        // displaying base stats on action bar
        task(period = 20L) {
            if (paper == null) {
                it.cancel()
                return@task
            }
            sendStatBar()
        }
    }

    var onAtsCooldown: Boolean = false

    var mainHand: MacrocosmItem?
        get() {
            val item = paper?.inventory?.itemInMainHand ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setItemInMainHand(value!!.build()) ?: Unit

    var offHand: MacrocosmItem?
        get() {
            val item = paper?.inventory?.itemInOffHand ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setItemInOffHand(value!!.build()) ?: Unit

    var helmet: MacrocosmItem?
        get() {
            val item = paper?.inventory?.helmet ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setHelmet(value!!.build()) ?: Unit

    var chestplate: MacrocosmItem?
        get() {
            val item = paper?.inventory?.chestplate ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setChestplate(value!!.build()) ?: Unit

    var leggings: MacrocosmItem?
        get() {
            val item = paper?.inventory?.leggings ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setLeggings(value!!.build()) ?: Unit

    var boots: MacrocosmItem?
        get() {
            val item = paper?.inventory?.boots ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setBoots(value!!.build()) ?: Unit

    private fun sendStatBar() {
        val stats = calculateStats()!!
        paper?.sendActionBar(comp("<red>${currentHealth.roundToInt()}/${stats.health.roundToInt()}❤    <green>${stats.defense.roundToInt()}❈ Defense    <aqua>${currentMana.roundToInt()}/${stats.intelligence.roundToInt()}✎ Mana"))
    }

    fun kill(reason: Component? = null) {
        if (paper == null)
            return

        purse /= 2f

        if (reason == null) {
            broadcast(comp("<red>☠ <gray>").append(paper!!.displayName().append(comp("<gray> died."))))
            if (purse > 0f) {
                paper!!.sendMessage(comp("<red>You died and lost ${Formatting.withCommas(purse.toBigDecimal())} coins!"))
            } else {
                paper!!.sendMessage(comp("<red>You died!"))
            }
        } else {
            broadcast(
                comp("<red>☠ <gray>").append(
                    paper!!.displayName().append(comp("<gray> was killed by ").append(reason))
                )
            )
            if (purse > 0f) {
                paper!!.sendMessage(
                    comp("<red>You were killed by ").append(reason)
                        .append(comp("<red> and lost ${Formatting.withCommas(purse.toBigDecimal())} coins!"))
                )
            } else {
                paper!!.sendMessage(comp("<red>You were killed by ").append(reason).append(comp("<red>!")))
            }
        }
        paper!!.teleport(paper!!.world.spawnLocation)
        currentHealth = calculateStats()?.health ?: baseStats.health
    }

    fun decreaseMana(amount: Float) {
        currentMana -= amount
        sendStatBar()
    }

    /**
     * Note that it deals raw damage, and does not reduce it further!
     */
    fun damage(amount: Float, source: Component? = null) {
        if (paper == null)
            return
        currentHealth -= amount
        if (currentHealth <= 0)
            kill(source)
        paper!!.health = (currentHealth / calculateStats()!!.health).toDouble() * 20
        sendStatBar()
    }

    fun calculateStats(): Statistics? {
        if (paper == null)
            return null

        val cloned = baseStats.clone()
        EquipmentSlot.values().forEach {
            val baseItem = paper!!.inventory.getItem(it)
            if (baseItem.type == Material.AIR)
                return@forEach
            val item = ItemRegistry.toMacrocosm(baseItem) ?: return@forEach
            cloned.increase(item.stats)
            cloned.multiply(1 + item.specialStats.statBoost)
            if(item.enchantments.isNotEmpty()) {
                for((ench, level) in item.enchantments) {
                    val base = ench.stats(level)
                    val special = ench.special(level)
                    cloned.increase(base)
                    cloned.multiply(1 + special.statBoost)
                }
            }
        }

        return cloned
    }

    fun specialStats(): SpecialStatistics? {
        val stats = SpecialStatistics()
        if(paper == null)
            return null

        EquipmentSlot.values().forEach {
            val baseItem = paper!!.inventory.getItem(it)
            if(baseItem.type == Material.AIR)
                return@forEach

            val item = ItemRegistry.toMacrocosm(baseItem) ?: return@forEach

            stats.increase(item.specialStats)
            if(item.enchantments.isNotEmpty()) {
                for((ench, level) in item.enchantments) {
                    val special = ench.special(level)
                    stats.increase(special)
                }
            }
        }

        return stats
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
