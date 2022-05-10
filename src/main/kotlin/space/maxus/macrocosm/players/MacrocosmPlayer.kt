package space.maxus.macrocosm.players

import com.google.gson.reflect.TypeToken
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.annotations.NotNull
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.collections.Collections
import space.maxus.macrocosm.damage.clamp
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.db.DatabaseStore
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.skills.Skills
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.Identifier
import java.sql.Statement
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min
import kotlin.math.roundToInt

val Player. macrocosm get() = Macrocosm.onlinePlayers[uniqueId]

@Suppress("unused", "ReplaceWithEnumMap")
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
    var lastAbilityUse: HashMap<Identifier, Long> = hashMapOf()
    var unlockedRecipes: MutableList<Identifier> = mutableListOf()
    var skills: Skills = Skills.default()
    var collections: Collections = Collections.default()
    var blockNextStatus: Boolean = false

    val activeEffects get() = paper?.activePotionEffects?.map { it.type }

    init {
        // statistic manipulations
        task(period = 40L) {
            if (paper == null) {
                it.cancel()
                return@task
            }
            val stats = calculateStats()!!
            if (currentMana < stats.intelligence)
                currentMana += stats.intelligence / 20f
            if (currentHealth < stats.health && !activeEffects!!.contains(PotionEffectType.ABSORPTION) && !activeEffects!!.contains(
                    PotionEffectType.POISON
                )
            ) {
                currentHealth = min(currentHealth + (stats.health / 20f), stats.health)
                paper!!.health = clamp((currentHealth / stats.health) * 20f, 0f, 20f).toDouble()
            }
            paper?.walkSpeed = 0.2F * (stats.speed / 100f)

            if(blockNextStatus)
                blockNextStatus = false
            else sendStatBar(stats)
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

    @Suppress("UNUSED_PARAMETER")
    fun isRecipeLocked(recipe: Identifier): Boolean {
        // todo: collections + skill checks
        return false
    }

    fun addSkillExperience(skill: SkillType, exp: Double) {
        skills.increase(skill, exp)
        blockNextStatus = true
        paper?.sendActionBar(comp("<aqua>+${Formatting.withCommas(exp.toBigDecimal(), true)} ${skill.inst.name} EXP"))
        sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
            pitch = 2f
            playFor(paper!!)
        }
        if(skill.inst.table.shouldLevelUp(skills.level(skill), skills[skill], exp)) {
            val lvl = skills.level(skill) + 1
            skills.setLevel(skill, lvl)
            sendSkillLevelUp(skill)
            skill.inst.rewards[lvl - 1].reward(this, lvl)
        }
    }

    fun addCollectionAmount(collection: CollectionType, amount: Int) {
        collections.increase(collection, amount)
        if(collection.inst.table.shouldLevelUp(collections.level(collection), collections[collection].toDouble(), amount.toDouble())) {
            val lvl = collections.level(collection) + 1
            collections.setLevel(collection, lvl)
            sendCollectionLevelUp(collection)
            collection.inst.rewards[lvl - 1].reward(this, lvl)
        }

    }

    fun sendSkillLevelUp(skill: SkillType) {
        val newLevel = skills.level(skill)
        val previous = newLevel - 1
        val roman = roman(newLevel)
        val message = comp("""<dark_aqua><bold>
--------------------------------------
 <aqua><bold>SKILL LEVEL UP!<!bold> <dark_aqua>${skill.inst.name} ${if(previous > 0) "<dark_gray>${roman(previous)}➜" else ""}<dark_aqua>$roman
  <yellow>${skill.profession} $roman
 ${skill.descript(newLevel)}<reset>
 ${skill.inst.rewards[newLevel - 1].display(newLevel).str()}
<dark_aqua><bold>--------------------------------------""".trimIndent())
        paper?.sendMessage(message)
        sound(Sound.ENTITY_PLAYER_LEVELUP) {
            playFor(paper!!)
        }
    }

    fun sendCollectionLevelUp(coll: CollectionType) {
        val newLevel = collections.level(coll)
        val previous = newLevel - 1
        val roman = roman(newLevel)
        val message = comp("""<gold><bold>
--------------------------------------</bold>
 <yellow><bold>COLLECTION LEVEL UP!<!bold> <gold>${coll.inst.name} ${if(previous > 0) "<dark_gray>${roman(previous)}➜" else ""}<gold>$roman
  ${coll.inst.rewards[newLevel - 1].display(newLevel).str()}
 <gold><bold>
--------------------------------------""".trimIndent())
        paper?.sendMessage(message)
        sound(Sound.ENTITY_PLAYER_LEVELUP) {
            playFor(paper!!)
        }
    }

    fun addAbsorption(amount: Float, length: Int = -1, myStats: Statistics? = null) {
        val stats = myStats ?: calculateStats()!!
        paper!!.addPotionEffect(
            PotionEffect(
                PotionEffectType.ABSORPTION,
                if (length < 0) Int.MAX_VALUE else length,
                2,
                true,
                true,
                true
            )
        )
        currentHealth = stats.health + amount
        sendStatBar(stats)
    }

    fun heal(amount: Float, myStats: Statistics? = null) {
        val stats = myStats ?: calculateStats()!!
        currentHealth = min(currentHealth + amount, stats.health)
        sendStatBar(stats)
    }

    fun sendMessage(message: String) {
        paper?.sendMessage(comp(message))
    }

    private fun sendStatBar(myStats: Statistics? = null) {
        val stats = myStats ?: calculateStats()!!
        val activeEffects = paper!!.activePotionEffects.map { it.type }
        val healthColor =
            if (activeEffects.contains(PotionEffectType.WITHER)) TextColor.color(0x2B0C0C) else if (activeEffects.contains(
                    PotionEffectType.ABSORPTION
                )
            ) NamedTextColor.GOLD else if (activeEffects.contains(PotionEffectType.POISON)) NamedTextColor.DARK_GREEN else NamedTextColor.RED

        paper?.sendActionBar(
            comp("${currentHealth.roundToInt()}/${stats.health.roundToInt()}❤    ").color(healthColor)
                .append(comp("<green>${stats.defense.roundToInt()}❈ Defense    <aqua>${currentMana.roundToInt()}/${stats.intelligence.roundToInt()}✎ Mana"))
        )
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
                    paper!!.displayName().append(comp("<gray> died because of ").append(reason))
                )
            )
            if (purse > 0f) {
                paper!!.sendMessage(
                    comp("<red>You died because of ").append(reason)
                        .append(comp("<red> and lost ${Formatting.withCommas(purse.toBigDecimal())} coins!"))
                )
            } else {
                paper!!.sendMessage(comp("<red>You died because of ").append(reason).append(comp("<red>!")))
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

        val stats = calculateStats()!!
        currentHealth -= amount
        if (currentHealth < stats.health) {
            paper!!.removePotionEffect(PotionEffectType.ABSORPTION)
        }

        if (currentHealth <= 0)
            kill(source)
        paper!!.health = min((currentHealth / stats.health).toDouble() * 20, 20.0)
        sendStatBar(stats)
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
            if (item.enchantments.isNotEmpty()) {
                for ((ench, level) in item.enchantments) {
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
        if (paper == null)
            return null

        EquipmentSlot.values().forEach {
            val baseItem = paper!!.inventory.getItem(it)
            if (baseItem.type == Material.AIR)
                return@forEach

            val item = ItemRegistry.toMacrocosm(baseItem) ?: return@forEach

            stats.increase(item.specialStats)
            if (item.enchantments.isNotEmpty()) {
                for ((ench, level) in item.enchantments) {
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
        val skillsJson = skills.json()
        val collectionJson = collections.json()
        val recipes = GSON.toJson(unlockedRecipes.map { it.toString() })
        stmt.executeUpdate("""INSERT OR REPLACE INTO SkillsCollections VALUES ('$ref', '$collectionJson', '$skillsJson')""")
        stmt.executeUpdate("""INSERT OR REPLACE INTO Recipes VALUES ('$ref', '$recipes')""")
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

            val skillsCollections = stmt.executeQuery("SELECT * FROM SkillsCollections WHERE UUID = '$id'")
            if(!skillsCollections.next()) return null
            val skills = Skills.fromJson(skillsCollections.getString("SKILLS"))
            val colls = Collections.fromJson(skillsCollections.getString("COLLECTIONS"))
            player.skills = skills
            player.collections = colls

            val recipesRes = stmt.executeQuery("SELECT * FROM Recipes WHERE UUID = '$id'")
            if(!recipesRes.next())
                return null
            val recipes = GSON.fromJson<List<String>>(recipesRes.getString("RECIPES"), object: TypeToken<List<String>>() { }.type).map { Identifier.parse(it) }
            player.unlockedRecipes = recipes.toMutableList()

            stmt.close()
            return player
        }
    }
}
