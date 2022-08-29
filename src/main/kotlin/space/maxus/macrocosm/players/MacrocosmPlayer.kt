package space.maxus.macrocosm.players

import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.annotations.NotNull
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.update
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.collections.Collections
import space.maxus.macrocosm.damage.clamp
import space.maxus.macrocosm.database
import space.maxus.macrocosm.db.*
import space.maxus.macrocosm.display.RenderPriority
import space.maxus.macrocosm.display.SidebarRenderer
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerDeathEvent
import space.maxus.macrocosm.events.PlayerTickEvent
import space.maxus.macrocosm.forge.ActiveForgeRecipe
import space.maxus.macrocosm.item.Items
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.pets.PetInstance
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.chat.ChatChannel
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.skills.Skills
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerQuest
import space.maxus.macrocosm.slayer.SlayerStatus
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.associateWithHashed
import space.maxus.macrocosm.util.fromJson
import space.maxus.macrocosm.util.ignoring
import space.maxus.macrocosm.util.toJson
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

val Player.macrocosm get() = Macrocosm.loadedPlayers[uniqueId]

@Suppress("unused", "ReplaceWithEnumMap")
class MacrocosmPlayer(val ref: UUID) : DatabaseStore {
    val paper: Player? get() = Bukkit.getServer().getPlayer(ref)

    var equipment: PlayerEquipment = PlayerEquipment()
    var rank: Rank = Rank.NONE
    var firstJoin: Long = Instant.now().toEpochMilli()
    var lastJoin: Long = Instant.now().toEpochMilli()
    var playtime: Long = 0
    var baseStats: Statistics = Statistics.default()
    var tempStats: Statistics = Statistics.zero()
    var tempSpecs: SpecialStatistics = SpecialStatistics()
    var purse: BigDecimal = BigDecimal(0)
    var bank: BigDecimal = BigDecimal(0)
    var currentHealth: Float = stats()?.health ?: 100f
    var currentMana: Float = stats()?.intelligence ?: 0f
    var lastAbilityUse: HashMap<Identifier, Long> = hashMapOf()
    var unlockedRecipes: MutableList<Identifier> = mutableListOf()
    var skills: Skills = Skills.default()
    var collections: Collections = Collections.default()
    var ownedPets: HashMap<String, StoredPet> = hashMapOf()
    var activePet: PetInstance? = null
    var slayerQuest: SlayerQuest? = null
    var slayers: HashMap<SlayerType, SlayerLevel> =
        SlayerType.values().asIterable().associateWithHashed(ignoring(SlayerLevel(0, .0, listOf(), .0)))
    var boundSlayerBoss: UUID? = null
    var summons: MutableList<UUID> = mutableListOf()
    var summonSlotsUsed: Int = 0
    var memory: PlayerMemory = PlayerMemory.nullMemory()
    var activeForgeRecipes: MutableList<ActiveForgeRecipe> = mutableListOf()
    var availableEssence: HashMap<EssenceType, Int> = EssenceType.values().asIterable().associateWithHashed(ignoring(0))

    private var slayerRenderId: UUID? = null
    private var statCache: Statistics? = null
    private var specialCache: SpecialStatistics? = null

    val activeEffects get() = paper?.activePotionEffects?.map { it.type }

    init {
        // statistic manipulations + ticking
        task(period = 40L) {
            if (paper == null) {
                it.cancel()
                return@task
            }
            recalculateSpecialStats()
            val stats = recalculateStats()

            PlayerTickEvent(this).callEvent()

            if (currentMana < stats.intelligence)
                currentMana += stats.intelligence / 20f
            if (currentHealth < stats.health && !activeEffects!!.contains(PotionEffectType.ABSORPTION) && !activeEffects!!.contains(
                    PotionEffectType.POISON
                )
            ) {
                currentMana = min(currentMana, stats.intelligence)
                currentHealth = min(currentHealth + (stats.health / 100f) + stats.vitality, stats.health)
                paper!!.health = clamp((currentHealth / stats.health) * 20f, 0f, 20f).toDouble()
            }
            paper?.walkSpeed = 0.2F * (stats.speed / 100f)

            sendStatBar(stats)
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
        set(@NotNull value) = paper?.inventory?.setItemInMainHand(value!!.build(this)) ?: Unit

    var offHand: MacrocosmItem?
        get() {
            val item = paper?.inventory?.itemInOffHand ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setItemInOffHand(value!!.build(this)) ?: Unit

    var helmet: MacrocosmItem?
        get() {
            val item = paper?.inventory?.helmet ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setHelmet(value!!.build(this)) ?: Unit

    var chestplate: MacrocosmItem?
        get() {
            val item = paper?.inventory?.chestplate ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setChestplate(value!!.build(this)) ?: Unit

    var leggings: MacrocosmItem?
        get() {
            val item = paper?.inventory?.leggings ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setLeggings(value!!.build(this)) ?: Unit

    var boots: MacrocosmItem?
        get() {
            val item = paper?.inventory?.boots ?: return null
            if (item.type == Material.AIR)
                return null
            return item.macrocosm
        }
        set(@NotNull value) = paper?.inventory?.setBoots(value!!.build(this)) ?: Unit

    fun startSlayerQuest(type: SlayerType, tier: Int) {
        val p = paper ?: return

        slayerQuest = SlayerQuest(type, tier, 0f, SlayerStatus.COLLECT_EXPERIENCE)
        val requiredExp = type.slayer.requiredExp[tier - 1]
        sound(Sound.ENTITY_ENDER_DRAGON_GROWL) {
            pitch = 2f
            playFor(p)
        }
        sendMessage("<dark_purple><bold>SLAYER QUEST STARTED!")
        sendMessage("<dark_purple>▶ <gray>Slay <red>${Formatting.withCommas(requiredExp.toBigDecimal())} Combat XP<gray> worth of <green>${type.slayer.entities}<gray> to summon the boss!")
        if (slayerRenderId != null)
            SidebarRenderer.dequeue(p, slayerRenderId!!)
        slayerRenderId = SidebarRenderer.enqueue(p, slayerQuest!!.render(), RenderPriority.LOW)
    }

    fun updateSlayerQuest(new: SlayerQuest) {
        val p = paper ?: return
        if (slayerQuest == null)
            return

        slayerQuest = new
        if (slayerRenderId != null)
            SidebarRenderer.dequeue(p, slayerRenderId!!)
        slayerRenderId = SidebarRenderer.enqueue(p, new.render(), RenderPriority.LOW)
    }

    fun isRecipeLocked(recipe: Identifier): Boolean {
        return !unlockedRecipes.contains(recipe)
    }

    fun addPet(type: Identifier, rarity: Rarity, level: Int, overflow: Double = .0): String {
        val stored = StoredPet(type, rarity, level, overflow)
        val key = "$type@${stored.hashCode().toString(16)}"
        if (ownedPets.containsKey(key))
            return key
        ownedPets[key] = stored
        return key
    }

    fun addSkillExperience(skill: SkillType, exp: Double) {
        val table = skill.inst.table
        val currentLevel = skills.level(skill)
        val required = table.expForLevel(currentLevel + 1)
        // overflow + added experience
        val current = skills[skill] + exp
        paper?.sendActionBar(
            text(
                "<aqua>+${
                    Formatting.withCommas(
                        exp.toBigDecimal(),
                        true
                    )
                } ${skill.inst.name} XP (${Formatting.withCommas(current.toBigDecimal())}${
                    if (currentLevel < skill.maxLevel) "/${
                        Formatting.withCommas(
                            required.toBigDecimal()
                        )
                    }" else ""
                })"
            )
        )
        sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
            pitch = 2f
            playFor(paper!!)
        }
        if (skills.increase(skill, exp)) {
            val lvl = skills.level(skill) + 1
            skills.setLevel(skill, lvl)
            skills[skill] = .0
            sendSkillLevelUp(skill)
            skill.inst.rewards[lvl - 1].reward(this, lvl)
        }

        // giving experience to pet
        activePet?.addExperience(this, exp, skill)
    }

    fun addCollectionAmount(collection: CollectionType, amount: Int) {
        collections.increase(collection, amount)
        if (collection.inst.table.shouldLevelUp(
                collections.level(collection),
                collections[collection].toDouble(),
                amount.toDouble()
            )
        ) {
            val lvl = collections.level(collection) + 1
            collections.setLevel(collection, lvl)
            // todo: rewards!!
            // sendCollectionLevelUp(collection)
            // collection.inst.rewards[lvl - 1].reward(this, lvl)
        }

    }

    fun sendSkillLevelUp(skill: SkillType) {
        // extra check just in case
        // we shouldnt be here tho
        if (skills.level(skill) >= 50)
            return
        val newLevel = skills.level(skill)
        val previous = newLevel - 1
        val roman = roman(newLevel)
        val message = text(
            """<dark_aqua><bold>
--------------------------------------
 <aqua><bold>SKILL LEVEL UP!<!bold> <dark_aqua>${skill.inst.name} ${if (previous > 0) "<dark_gray>${roman(previous)}➜" else ""}<dark_aqua>$roman
  <yellow>${skill.profession} $roman
 ${skill.descript(newLevel)}<reset>
 ${skill.inst.rewards[newLevel - 1].display(newLevel).str()}
<dark_aqua><bold>--------------------------------------""".trimIndent()
        )
        paper?.sendMessage(message)
        sound(Sound.ENTITY_PLAYER_LEVELUP) {
            playFor(paper!!)
        }
    }

    fun sendCollectionLevelUp(coll: CollectionType) {
        val newLevel = collections.level(coll)
        val previous = newLevel - 1
        val roman = roman(newLevel)
        val message = text(
            """<gold><bold>
--------------------------------------</bold>
 <yellow><bold>COLLECTION LEVEL UP!<!bold> <gold>${coll.inst.name} ${if (previous > 0) "<dark_gray>${roman(previous)}➜" else ""}<gold>$roman
  ${coll.inst.rewards[newLevel - 1].display(newLevel).str()}
 <gold><bold>
--------------------------------------""".trimIndent()
        )
        paper?.sendMessage(message)
        sound(Sound.ENTITY_PLAYER_LEVELUP) {
            playFor(paper!!)
        }
    }

    fun addAbsorption(amount: Float, length: Int = -1, myStats: Statistics? = null) {
        val stats = myStats ?: stats()!!
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
        val stats = myStats ?: stats()!!
        currentHealth = min(currentHealth + amount, stats.health)
        sendStatBar(stats)
    }

    fun sendMessage(message: String) {
        paper?.sendMessage(text(message))
    }

    fun sendMessage(channel: ChatChannel, message: String) {
        // todo: do channel block check
        sendMessage("${channel.prefix} $message")
    }

    private fun sendStatBar(myStats: Statistics? = null) {
        val stats = myStats ?: stats()!!
        val activeEffects = paper!!.activePotionEffects.map { it.type }
        val healthColor =
            if (activeEffects.contains(PotionEffectType.WITHER)) TextColor.color(0x2B0C0C) else if (activeEffects.contains(
                    PotionEffectType.ABSORPTION
                )
            ) NamedTextColor.GOLD else if (activeEffects.contains(PotionEffectType.POISON)) NamedTextColor.DARK_GREEN else NamedTextColor.RED

        paper?.sendActionBar(
            text("${currentHealth.roundToInt()}/${stats.health.roundToInt()}${Statistic.HEALTH.display}   ").color(
                healthColor
            )
                .append(text("<green>${stats.defense.roundToInt()}${Statistic.DEFENSE.display}    <aqua>${currentMana.roundToInt()}/${stats.intelligence.roundToInt()}✎ Mana"))
        )
    }

    fun kill(source: Component? = null) {
        if (paper == null)
            return

        val event = PlayerDeathEvent(this, source, purse / 2f.toBigDecimal())
        if (!event.callEvent()) {
            sound(Sound.ENTITY_ITEM_BREAK) {
                pitch = 0f
                playAt(paper!!.location)
            }
            return
        }

        val reason = event.source
        purse -= event.reduceCoins

        if (reason == null) {
            broadcast(text("<red>☠ <gray>").append(paper!!.displayName().append(text("<gray> died."))))
            if (purse > 0f.toBigDecimal()) {
                paper!!.sendMessage(text("<red>You died and lost ${Formatting.withCommas(event.reduceCoins)} coins!"))
            } else {
                paper!!.sendMessage(text("<red>You died!"))
            }
        } else {
            broadcast(
                text("<red>☠ <gray>").append(
                    paper!!.displayName().append(text("<gray> died because of ").append(reason))
                )
            )
            if (event.reduceCoins > 0f.toBigDecimal()) {
                paper!!.sendMessage(
                    text("<red>You died because of ").append(reason)
                        .append(text("<red> and lost ${Formatting.withCommas(event.reduceCoins)} coins!"))
                )
            } else {
                paper!!.sendMessage(text("<red>You died because of ").append(reason).append(text("<red>!")))
            }
        }
        paper!!.teleport(paper!!.world.spawnLocation)
        currentHealth = stats()?.health ?: baseStats.health
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

        val stats = stats()!!
        currentHealth -= amount
        if (currentHealth < stats.health) {
            paper!!.removePotionEffect(PotionEffectType.ABSORPTION)
        }

        if (currentHealth <= 0) {
            currentHealth = 0f
            kill(source)
        }
        paper!!.health = max(min((currentHealth / stats.health).toDouble() * 20, 20.0), .1)
        sendStatBar(stats)
    }

    private fun recalculateStats(): Statistics {
        val cloned = baseStats.clone()
        EquipmentSlot.values().forEach {
            val baseItem = paper!!.inventory.getItem(it)
            if (baseItem.type == Material.AIR)
                return@forEach
            val item = Items.toMacrocosm(baseItem) ?: return@forEach

            if (
                (it == EquipmentSlot.OFF_HAND && !item.type.leftHand) ||
                (item.type.armor && it.name.contains("HAND")) ||
                item.type.equipment
            )
                return@forEach
            cloned.increase(item.stats(this))
        }
        if (activePet != null) {
            cloned.increase(activePet!!.prototype.stats(activePet!!.level(this), activePet!!.rarity(this)))
        }
        for (item in equipment.enumerate()) {
            if (item != null) {
                cloned.increase(item.stats(this))
            }
        }
        if (specialCache != null && specialCache!!.statBoost != 0f) {
            cloned.multiply(1 + specialCache!!.statBoost)
        }

        val event = PlayerCalculateStatsEvent(this, cloned)
        event.callEvent()

        val cache = event.stats.clone()
        val capBoost = if (specialCache != null) {
            specialCache!!.speedCapBoost
        } else 0f
        cache.speed = min(400 + capBoost, cache.speed)
        statCache = cache.clone()

        return cache
    }

    private fun recalculateSpecialStats(): SpecialStatistics {
        val stats = SpecialStatistics()

        EquipmentSlot.values().forEach {
            val baseItem = paper!!.inventory.getItem(it)
            if (baseItem.type == Material.AIR)
                return@forEach

            val item = Items.toMacrocosm(baseItem) ?: return@forEach

            stats.increase(item.specialStats)
            if (item.enchantments.isNotEmpty()) {
                for ((ench, level) in item.enchantments) {
                    val special = ench.special(level)
                    stats.increase(special)
                }
            }
        }
        if (activePet != null) {
            stats.increase(activePet!!.prototype.specialStats(activePet!!.level(this), activePet!!.rarity(this)))
        }

        stats.increase(tempSpecs)

        val e = PlayerCalculateSpecialStatsEvent(this, stats)
        e.callEvent()
        specialCache = e.stats.clone()

        return e.stats.clone()
    }

    fun stats(): Statistics? {
        if (paper == null)
            return null

        val cache = statCache?.clone() ?: recalculateStats()
        cache.increase(tempStats)
        return cache
    }

    fun specialStats(): SpecialStatistics? {
        if (paper == null)
            return null

        return specialCache ?: recalculateSpecialStats()
    }

    override fun storeSelf(data: DataStorage) {
        val player = paper
        if (player == null) {
            println("Tried to store offline player $ref")
            return
        }
        val p = this
        data.transact {
            if (PlayersTable.update({ PlayersTable.uuid eq p.ref }) {
                    dump(it, p, false)
                } <= 0) {
                PlayersTable.insert {
                    dump(it, p, true)
                }
                StatsTable.insert {
                    it[uuid] = p.ref
                    it[StatsTable.data] = toJson(baseStats)
                }
            } else {
                StatsTable.update {
                    it[StatsTable.data] = toJson(baseStats)
                }
            }
        }

//        stmt.executeUpdate(
//            "INSERT OR REPLACE INTO Players VALUES ('$ref', ${rank.id()}, $firstJoin, $lastJoin, $newPlaytime, $purse, $bank, '${
//                GSON.toJson(
//                    this.memory
//                )
//            }', '${GSON.toJson(this.activeForgeRecipes)}')"
//        )
//        var leftHand = "INSERT OR REPLACE INTO Stats(UUID"
//        var rightHand = "VALUES ('$ref'"
//        for ((k, value) in baseStats.iter()) {
//            leftHand += ", ${k.name}"
//            rightHand += ", $value"
//        }
//        stmt.executeUpdate("$leftHand)$rightHand)")
//        val skillsJson = skills.json()
//        val collectionJson = collections.json()
//        val recipes = GSON.toJson(unlockedRecipes.map { it.toString() })
//        stmt.executeUpdate("""INSERT OR REPLACE INTO SkillsCollections VALUES ('$ref', '$collectionJson', '$skillsJson')""")
//        stmt.executeUpdate("""INSERT OR REPLACE INTO Recipes VALUES ('$ref', '$recipes')""")
//
//        val active = if (activePet != null) {
//            activePet!!.hashKey
//        } else ""
//        val pets = GSON.toJson(ownedPets)
//
//        stmt.executeUpdate("""INSERT OR REPLACE INTO Pets VALUES ('$ref', '$active', '$pets')""")
//
//        // slayers
//        val slayers = GSON.toJson(slayers.mapKeys { (k, _) -> k.name })
//        stmt.executeUpdate("""INSERT OR REPLACE INTO Slayers VALUES ('$ref', '$slayers')""")
//
//        // equipment
//        val equipment =
//            this.equipment.enumerate().joinToString(separator = ", ") { "'${it?.serializeToBytes(this) ?: "NULL"}'" }
//        stmt.executeUpdate("""INSERT OR REPLACE INTO Equipment VALUES ('$ref', $equipment)""")
//
//        // essence
//        val essence = GSON.toJson(this.availableEssence)
//        stmt.executeUpdate("""INSERT OR REPLACE INTO Essence VALUES ('$ref', '$essence')""")
//
//        stmt.close()
    }

    private fun dump(it: UpdateBuilder<*>, p: MacrocosmPlayer, id: Boolean) {
        val newPlaytime = playtime + (Instant.now().toEpochMilli() - lastJoin)
        if (id)
            it[PlayersTable.uuid] = p.ref
        it[PlayersTable.rank] = p.rank.id()
        it[PlayersTable.firstJoin] = p.firstJoin
        it[PlayersTable.lastJoin] = p.lastJoin
        it[PlayersTable.playtime] = newPlaytime
        it[PlayersTable.purse] = p.purse
        it[PlayersTable.bank] = p.bank
        it[PlayersTable.memory] = toJson(p.memory)
        it[PlayersTable.forge] = toJson(p.activeForgeRecipes)
        it[PlayersTable.collections] = p.collections.json()
        it[PlayersTable.skills] = p.skills.json()
        it[PlayersTable.recipes] = toJson(p.unlockedRecipes)
        it[PlayersTable.necklace] = if (p.equipment.necklace == null) "NULL" else toJson(p.equipment.necklace)
        it[PlayersTable.cloak] = if (p.equipment.cloak == null) "NULL" else toJson(p.equipment.cloak)
        it[PlayersTable.belt] = if (p.equipment.belt == null) "NULL" else toJson(p.equipment.belt)
        it[PlayersTable.gloves] = if (p.equipment.gloves == null) "NULL" else toJson(p.equipment.gloves)
        it[PlayersTable.slayers] = toJson(p.slayers)
        it[PlayersTable.activePet] = p.activePet?.hashKey ?: ""
        it[PlayersTable.pets] = toJson(p.ownedPets)
        it[PlayersTable.essence] = toJson(availableEssence)
    }

    fun playtimeMillis() = playtime + (Instant.now().toEpochMilli() - lastJoin)
    fun <T, L> sendPacket(packet: T) where T : Packet<L>, L : PacketListener {
        (this.paper as CraftPlayer).handle.networkManager.send(packet)
    }

    companion object {
        fun loadPlayer(id: UUID): MacrocosmPlayer? {
            if (Macrocosm.loadedPlayers.containsKey(id))
                return Macrocosm.loadedPlayers[id]
            val sql = database.transact {
                PlayersTable.select { PlayersTable.uuid eq id }.map { SqlPlayerData.fromRes(it) }.firstOrNull()
            } ?: return null

            val player = MacrocosmPlayer(id)
            player.rank = sql.rank
            player.firstJoin = sql.firstJoin
            player.lastJoin = Instant.now().toEpochMilli()
            player.playtime = sql.playtime
            player.purse = sql.purse
            player.bank = sql.bank
            player.memory = sql.memory
            player.activeForgeRecipes = sql.forge.toMutableList()
            player.collections = sql.collections
            player.skills = sql.skills
            player.unlockedRecipes = sql.recipes.toMutableList()
            player.equipment = sql.equipment
            player.slayers = sql.slayerExp
            player.ownedPets = sql.pets
            if (sql.activePet.isNotEmpty()) {
                val pet = player.ownedPets[sql.activePet]!!
                // delaying spawning pet, to prevent weird bugs
                task(delay = 20L) {
                    player.activePet = Registry.PET.find(pet.id).spawn(player, sql.activePet)
                }
            }
            player.availableEssence = sql.essence

            val stats = database.transact {
                StatsTable.select { StatsTable.uuid eq id }.map { it[StatsTable.data] }.firstOrNull()
            } ?: return null
            player.baseStats = fromJson(stats) ?: return null
            return player

//            val stats = stmt.executeQuery("SELECT * FROM Stats WHERE UUID = '$id'")
//            if (!stats.next())
//                return null
//            player.baseStats = Statistics.fromRes(stats)
//
//            val skillsCollections = stmt.executeQuery("SELECT * FROM SkillsCollections WHERE UUID = '$id'")
//            if (!skillsCollections.next()) return null
//            val skills = Skills.fromJson(skillsCollections.getString("SKILLS"))
//            val colls = Collections.fromJson(skillsCollections.getString("COLLECTIONS"))
//            player.skills = skills
//            player.collections = colls
//
//            val recipesRes = stmt.executeQuery("SELECT * FROM Recipes WHERE UUID = '$id'")
//            if (!recipesRes.next())
//                return null
//            val recipes =
//                GSON.fromJson<List<String>>(recipesRes.getString("RECIPES"), object : TypeToken<List<String>>() {}.type)
//                    .map { Identifier.parse(it) }
//            player.unlockedRecipes = recipes.toMutableList()
//
//            // pets
//            val petsRes = stmt.executeQuery("SELECT * FROM Pets WHERE UUID = '$id'")
//            if (!petsRes.next())
//                return null
//            player.ownedPets =
//                GSON.fromJson(petsRes.getString("PETS"), object : TypeToken<HashMap<String, StoredPet>>() {}.type)
//            val active = petsRes.getString("ACTIVE_PET")
//            if (active.isNotEmpty()) {
//                val pet = player.ownedPets[active]!!
//                // delaying spawning pet, to prevent weird bugs
//                task(delay = 20L) {
//                    player.activePet = Registry.PET.find(pet.id).spawn(player, active)
//                }
//            }
//
//            // slayers
//            val slayerRes = stmt.executeQuery("SELECT * FROM Slayers WHERE UUID = '$id'")
//            if (!slayerRes.next())
//                return null
//            val exp = GSON.fromJson<HashMap<String, SlayerLevel>>(
//                slayerRes.getString("EXPERIENCE"),
//                object : TypeToken<HashMap<String, SlayerLevel>>() {}.type
//            )
//            player.slayers = HashMap(exp.mapKeys { (k, _) -> SlayerType.valueOf(k) })
//
//            // equipment
//            val eqRes = stmt.executeQuery("SELECT * FROM Equipment WHERE UUID = '$id'")
//            if (!eqRes.next())
//                return null
//            val equipment = PlayerEquipment()
//            listOf(ItemType.NECKLACE, ItemType.CLOAK, ItemType.BELT, ItemType.GLOVES).associateWith {
//                val contained = eqRes.getString(it.name)
//                if (contained == "NULL")
//                    null
//                else
//                    MacrocosmItem.deserializeFromBytes(contained)
//            }.forEach { (ty, item) ->
//                equipment[ty] = item
//            }
//            player.equipment = equipment
//
//            // essence
//            val esRes = stmt.executeQuery("SELECT * FROM Essence WHERE UUID = '$id'")
//            if(!esRes.next())
//                return null
//            player.availableEssence = GSON.fromJson(esRes.getString("ESSENCE"), object: TypeToken<HashMap<EssenceType, Int>>() { }.type)
//
//            stmt.close()
        }
    }

}
