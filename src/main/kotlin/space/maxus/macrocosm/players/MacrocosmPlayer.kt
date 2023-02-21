package space.maxus.macrocosm.players

import com.mongodb.client.model.UpdateOptions
import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.annotations.NotNull
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.updateOne
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.accessory.AccessoryBag
import space.maxus.macrocosm.area.Area
import space.maxus.macrocosm.area.AreaType
import space.maxus.macrocosm.area.RestrictedArea
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.collections.CollectionCompound
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.damage.clamp
import space.maxus.macrocosm.discord.emitters.DevEnvironGoalEmitter
import space.maxus.macrocosm.discord.emitters.HighSkillEmitter
import space.maxus.macrocosm.display.RenderPriority
import space.maxus.macrocosm.display.SidebarRenderer
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.events.*
import space.maxus.macrocosm.forge.ActiveForgeRecipe
import space.maxus.macrocosm.item.Items
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.metrics.MacrocosmMetrics
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.MongoDb
import space.maxus.macrocosm.mongo.Store
import space.maxus.macrocosm.mongo.data.MongoActiveForgeRecipe
import space.maxus.macrocosm.mongo.data.MongoPlayerData
import space.maxus.macrocosm.npc.shop.ShopHistory
import space.maxus.macrocosm.pets.PetInstance
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.chat.ChatChannel
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.skills.Skills
import space.maxus.macrocosm.slayer.*
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUIInstance
import space.maxus.macrocosm.util.associateWithHashed
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.ignoring
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

val Player.macrocosm get() = Macrocosm.loadedPlayers[uniqueId]

@Suppress("unused")
class MacrocosmPlayer(val ref: UUID) : Store, MongoConvert<MongoPlayerData> {
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
    var collections: CollectionCompound = CollectionCompound.default()
    var ownedPets: HashMap<String, StoredPet> = hashMapOf()
    var activePet: PetInstance? = null
    var slayerQuest: SlayerQuest? = null
    var slayers: HashMap<SlayerType, SlayerLevel> =
        SlayerType.values().asIterable().associateWithHashed(
            ignoring(
                SlayerLevel(
                    0,
                    .0,
                    listOf(),
                    HashMap(SlayerType.values().associateWith { RngStatus(.0, -1) })
                )
            )
        )
    var boundSlayerBoss: UUID? = null
    var summons: MutableList<UUID> = mutableListOf()
    var summonSlotsUsed: Int = 0
    var memory: PlayerMemory = PlayerMemory.nullMemory()
    var activeForgeRecipes: MutableList<ActiveForgeRecipe> = mutableListOf()
    var availableEssence: HashMap<EssenceType, Int> = EssenceType.values().asIterable().associateWithHashed(ignoring(0))
    var accessoryBag: AccessoryBag = AccessoryBag()
    var goals: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
    var shopHistory: ShopHistory = ShopHistory(16, mutableListOf())
    var achievements: MutableList<Identifier> = mutableListOf()
    var achievementExp: Int = 0
    var area: Area = AreaType.OVERWORLD.area; private set

    var openUi: MacrocosmUIInstance? = null
    val uiHistory: MutableList<Identifier> = mutableListOf()

    private var slayerRenderId: UUID? = null
    var statCache: Statistics? = null; private set
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
                currentMana += stats.vigor
            if (currentHealth < stats.health && !activeEffects!!.contains(PotionEffectType.ABSORPTION) && !activeEffects!!.contains(
                    PotionEffectType.POISON
                )
            ) {
                currentHealth = min(currentHealth + stats.vitality, stats.health)
                paper!!.health = clamp((currentHealth / stats.health) * 20f, 0f, 20f).toDouble()
            }
            currentMana = min(currentMana, stats.intelligence)
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

    fun calculateZone(): Area {
        val p = paper ?: return AreaType.NONE.area
        val old = area
        val zone = Registry.AREA.iter().values.lastOrNull { it.contains(p.location) } ?: AreaType.OVERWORLD.area
        if (old.id != zone.id) {
            // We have entered a new zone
            val event = PlayerEnterAreaEvent(this, p, zone, old, !goals.contains("area.${zone.id.path}"))
            zone.model.onEnter(event)
            if (event.isCancelled || !event.callEvent()) {
                // The event was cancelled, the player can not enter the zone yet
                if (zone is RestrictedArea) {
                    // Teleport the player away
                    sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                        pitch = 0f
                        volume = 3f
                        playFor(p)
                    }
                    p.teleport(zone.exit)
                    return old
                }
                return old
            }

            // Everything is fine
            if (event.firstEnter) {
                // Player has entered a new area!
                reachGoal("area.${zone.id.path}")
                zone.model.announce(p)
            }
        }
        this.area = zone
        return zone
    }

    /**
     * Gives player achievement with the provided ID
     */
    fun giveAchievement(achievement: Identifier) {
        val ach = Registry.ACHIEVEMENT.find(achievement)
        if (!this.achievements.contains(achievement)) {
            this.achievements.add(achievement)
            this.achievementExp += ach.expAwarded
            ach.award(this)
        }
    }

    /**
     * Gives player achievement with the provided ID in string form
     */
    fun giveAchievement(achievement: String) = giveAchievement(Identifier.parse(achievement))

    fun startSlayerQuest(type: SlayerType, tier: Int) {
        val p = paper ?: return

        MacrocosmMetrics.counter("slayer_${type.name.lowercase()}").inc()
        slayerQuest = SlayerQuest(
            type,
            tier,
            0f,
            SlayerStatus.COLLECT_EXPERIENCE,
            MacrocosmMetrics.summary("slayer_time", "Time spent doing a slayer quest", .95, .05).startTimer()
        )
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
                collections[collection]
            ) && !collections.isMaxLevel(collection)
        ) {
            val lvl = collections.level(collection) + 1
            collections.setLevel(collection, lvl)
            sendCollectionLevelUp(collection)
            collection.inst.rewards[lvl - 1].reward(this, lvl)
        }
    }

    fun sendSkillLevelUp(skill: SkillType) {
        // extra check just in case
        // we shouldn't be here tho
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

        if (newLevel > 45) {
            Threading.runAsync {
                // trying to emit the macrocosm:high_skill event
                Registry.DISCORD_EMITTERS.tryUse(id("high_skill")) { emitter ->
                    (emitter as HighSkillEmitter).post(HighSkillEmitter.Context(skill, newLevel, this))
                }
            }
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

    fun configuredMiniMessage(): TagResolver {
        return TagResolver.resolver(
            TagResolver.caching(
                TagResolver.resolver(
                    "player_name",
                    Tag.inserting(text("<yellow>${paper!!.name}</yellow>"))
                )
            )
        )
    }

    fun sendMessage(message: String) {
        paper?.sendMessage(MiniMessage.miniMessage().deserialize(message, configuredMiniMessage()))
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

    fun reachGoal(goal: String) {
        if (!goals.contains(goal)) {
            task(sync = false) {
                // Running the event asynchronously
                val event = PlayerReachGoalEvent(this, goal)
                event.callEvent()
                this.goals.add(goal)
                if (Macrocosm.isInDevEnvironment)
                    Registry.DISCORD_EMITTERS.tryUse(id("goal_reached")) { emitter ->
                        (emitter as DevEnvironGoalEmitter).post(DevEnvironGoalEmitter.Context(this, goal))
                    }
            }
        }
    }

    fun hasReachedGoal(goal: String): Boolean {
        return goals.contains(goal)
    }

    private fun recalculateStats(): Statistics {
        val cloned = baseStats.clone()
        // vitality + vigor
        cloned.vitality += (currentHealth / 100f)
        cloned.vigor += (currentMana / 50f)
        @Suppress("SENSELESS_COMPARISON") // NPEs actually do happen on player join
        if (accessoryBag != null) {
            for (accessory in accessoryBag.accessories) {
                val item = Registry.ITEM.find(accessory.item)
                if (item.rarity != accessory.rarity) {
                    item.rarity = accessory.rarity
                    item.rarityUpgraded = true
                }
                cloned.increase(item.stats(this))
            }
            val power = Registry.ACCESSORY_POWER.findOrNull(accessoryBag.power)
            if (power != null) {
                val stats = power.stats.clone()
                stats.multiply(accessoryBag.statModifier().toFloat())
                cloned.increase(stats)
            }
        }
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
                    val actualEnch = Registry.ENCHANT.find(ench)
                    val special = actualEnch.special(level)
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

    override fun store() {
        MongoDb.players.updateOne(
            MongoPlayerData::uuid eq this.ref,
            this.mongo,
            UpdateOptions().upsert(true)
        )
        activePet?.despawn(this)
    }

    fun playtimeMillis() = playtime + (Instant.now().toEpochMilli() - lastJoin)
    fun <T, L> sendPacket(packet: T) where T : Packet<L>, L : PacketListener {
        (this.paper as CraftPlayer).handle.connection.send(packet)
    }

    companion object {
        fun loadOrInit(id: UUID): MacrocosmPlayer {
            val loaded = loadPlayer(id)
            if (loaded != null)
                return loaded
            val player = MacrocosmPlayer(id)
            Macrocosm.playersLazy.add(id)
            Macrocosm.loadedPlayers[id] = player
            async {
                player.store()
            }
            return player
        }

        fun loadPlayer(uuid: UUID): MacrocosmPlayer? {
            if (Macrocosm.loadedPlayers.containsKey(uuid))
                return Macrocosm.loadedPlayers[uuid]
            return loadPlayer(MongoDb.players.findOne(MongoPlayerData::uuid eq uuid) ?: return null)
        }

        fun loadPlayer(mongo: MongoPlayerData): MacrocosmPlayer {
            val player = MacrocosmPlayer(mongo.uuid)
            player.rank = mongo.rank
            player.firstJoin = mongo.firstJoin
            player.lastJoin = mongo.lastJoin
            player.playtime = mongo.playtime
            player.purse = mongo.purse
            player.bank = mongo.bank
            player.memory = mongo.memory.actual
            player.activeForgeRecipes = mongo.forge.map(MongoActiveForgeRecipe::actual).toMutableList()
            player.collections = mongo.collections
            player.skills = mongo.skills
            player.unlockedRecipes = mongo.unlockedRecipes.map(Identifier::parse).toMutableList()
            player.equipment = mongo.equipment.actual
            player.slayers = mongo.slayers
            player.ownedPets = HashMap(mongo.ownedPets.map { it.key to it.value.actual }.toMap())
            player.goals = ConcurrentLinkedQueue(mongo.goals)
            if (mongo.activePet.isNotBlank()) {
                val pet = player.ownedPets[mongo.activePet]!!
                task(delay = 20L) {
                    player.activePet = Registry.PET.find(pet.id).spawn(player, mongo.activePet)
                }
            }
            player.availableEssence = mongo.essence
            player.accessoryBag = mongo.accessories.actual
            player.baseStats =
                Statistics(TreeMap(mongo.baseStats.map { Statistic.valueOf(it.key) to it.value }.toMap()))
            player.shopHistory = mongo.shopHistory?.actual ?: ShopHistory(16, mutableListOf())
            player.achievements = mongo.achievements?.map { Identifier.parse(it) }?.toMutableList() ?: mutableListOf()
            player.achievementExp = mongo.achievementExp ?: 0

            return player
        }
    }

    override val mongo: MongoPlayerData
        get() = MongoPlayerData(
            ref,
            equipment.mongo(this),
            rank,
            firstJoin,
            lastJoin,
            playtime,
            baseStats.iter().map { it.key.name to it.value }.toMap(),
            purse,
            bank,
            skills,
            collections,
            HashMap(ownedPets.map { it.key to it.value.mongo }.toMap()),
            activePet?.hashKey ?: "",
            memory.mongo,
            activeForgeRecipes.map(ActiveForgeRecipe::mongo),
            unlockedRecipes.map(Identifier::toString),
            slayers,
            availableEssence,
            accessoryBag.mongo,
            goals.toList(),
            shopHistory.mongo,
            achievements.map { it.toString() },
            achievementExp
        )

}
