package space.maxus.macrocosm

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import net.axay.kspigot.extensions.worlds
import net.axay.kspigot.main.KSpigot
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.api.KeyManager
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.bazaar.Bazaar
import space.maxus.macrocosm.bazaar.BazaarElement
import space.maxus.macrocosm.block.CustomBlockHandlers
import space.maxus.macrocosm.block.MacrocosmBlock
import space.maxus.macrocosm.commands.*
import space.maxus.macrocosm.cosmetic.Cosmetics
import space.maxus.macrocosm.datagen.DataGenerators
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.db.DataStorage
import space.maxus.macrocosm.db.impl.postgres.PostgresDatabaseImpl
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.display.SidebarRenderer
import space.maxus.macrocosm.enchants.Enchant
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.events.ServerShutdownEvent
import space.maxus.macrocosm.fishing.FishingHandler
import space.maxus.macrocosm.fishing.SeaCreatures
import space.maxus.macrocosm.fishing.TrophyFishes
import space.maxus.macrocosm.forge.ForgeRecipe
import space.maxus.macrocosm.generators.*
import space.maxus.macrocosm.item.Armor
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.buffs.Buffs
import space.maxus.macrocosm.item.json.ItemParser
import space.maxus.macrocosm.item.runes.StatRune
import space.maxus.macrocosm.listeners.*
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.vanilla
import space.maxus.macrocosm.mining.MiningHandler
import space.maxus.macrocosm.net.MacrocosmServer
import space.maxus.macrocosm.pack.PackDescription
import space.maxus.macrocosm.pack.PackProvider
import space.maxus.macrocosm.pets.PetValue
import space.maxus.macrocosm.pets.types.PyroclasticToadPet
import space.maxus.macrocosm.pets.types.WaspPet
import space.maxus.macrocosm.players.EquipmentHandler
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.banking.TransactionHistory
import space.maxus.macrocosm.recipes.RecipeMenu
import space.maxus.macrocosm.recipes.RecipeValue
import space.maxus.macrocosm.reforge.ReforgeType
import space.maxus.macrocosm.registry.Clone
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.AlchemyReward
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.SlayerHandlers
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.zombie.ZombieAbilities
import space.maxus.macrocosm.spell.SpellValue
import space.maxus.macrocosm.spell.essence.ScrollRecipe
import space.maxus.macrocosm.util.annotations.UnsafeFeature
import space.maxus.macrocosm.util.data.SemanticVersion
import space.maxus.macrocosm.util.data.Unsafe
import space.maxus.macrocosm.util.fromJson
import space.maxus.macrocosm.util.game.Calendar
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.walkDataResources
import space.maxus.macrocosm.workarounds.AsyncLauncher
import space.maxus.macrocosm.zone.ZoneType
import java.awt.Font
import java.net.URL
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import kotlin.io.path.*
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(UnsafeFeature::class)
class InternalMacrocosmPlugin : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMacrocosmPlugin; private set
        lateinit var PACKET_MANAGER: ProtocolManager; private set
        lateinit var UNSAFE: Unsafe; private set
        lateinit var DATABASE: DataStorage; private set
        lateinit var TRANSACTION_HISTORY: TransactionHistory

        lateinit var FONT_MINECRAFT: Font; private set
        lateinit var FONT_MINECRAFT_BOLD: Font; private set
        lateinit var FONT_MINECRAFT_ITALIC: Font; private set

        private data class VersionInfo(val version: String, val apiVersion: String)
    }

    val constantProfileId: UUID = UUID.fromString("13e76730-de52-4197-909a-6d50e0a2203b")
    val id: String = "macrocosm"
    val loadedPlayers: HashMap<UUID, MacrocosmPlayer> = hashMapOf()
    val version by lazy { MacrocosmConstants.VERSION }
    val apiVersion by lazy { MacrocosmConstants.API_VERSION }
    val transactionHistory by lazy { TRANSACTION_HISTORY }
    var isSandbox: Boolean = false; private set
    var isInDevEnvironment: Boolean = false; private set
    val random: Random = Random(ThreadLocalRandom.current().nextLong())
    val macrocosmColor: TextColor = TextColor.color(0x4A26BB)
    val isOnline by lazy { !MacrocosmConstants.OFFLINE_MODE }
    lateinit var integratedServer: MacrocosmServer; private set
    lateinit var playersLazy: MutableList<UUID>; private set

    override fun load() {
//        SignatureDissolver.inject()
        try {
            val conn = URL("https://api.ipify.org").openConnection() as HttpsURLConnection
            MacrocosmConstants.CURRENT_IP = conn.inputStream.readAllBytes().decodeToString()
            conn.disconnect()
        } catch (e: Exception) {
            // we are probably offline, set the current ip to localhost
            MacrocosmConstants.CURRENT_IP = "127.0.0.1"
        }
        MacrocosmConstants.OFFLINE_MODE = !server.onlineMode
        isInDevEnvironment = java.lang.Boolean.getBoolean("macrocosm.dev")
        integratedServer =
            MacrocosmServer((if (isInDevEnvironment) "devMini" else "mini") + Random.nextBytes(1)[0].toString(16))
        INSTANCE = this
        UNSAFE = Unsafe(Random.nextInt())
        Accessor.init()
        val versionInfo: VersionInfo = fromJson(
            Charsets.UTF_8.decode(ByteBuffer.wrap(this.getResource("MACROCOSM_VERSION_INFO")!!.readAllBytes()))
                .toString()
        )!!
        MacrocosmConstants.API_VERSION = SemanticVersion.fromString(versionInfo.apiVersion)
        MacrocosmConstants.VERSION = SemanticVersion.fromString(versionInfo.version)
        KeyManager.load()
        BazaarElement.init()
        Threading.contextBoundedRunAsync {
            info("Starting REST API Server")
            AsyncLauncher.launchApi()
        }
        Threading.runAsync {
            DATABASE =
//                if (isInDevEnvironment)
//                    SqliteDatabaseImpl
//                else
                PostgresDatabaseImpl(System.getProperty("macrocosm.postgres.remote"))
            DATABASE.connect()
            playersLazy = DATABASE.readPlayers().toMutableList()
        }
        Threading.runAsync {
            val rendersDir = Accessor.access("item_renders")
            if (!rendersDir.exists())
                rendersDir.createDirectories()
            // Setting up fonts
            walkDataResources("fonts") { path ->
                val out = Accessor.access("fonts")
                if (!out.exists())
                    out.createDirectories()
                val filePath = out.resolve(path.name)
                path.copyTo(filePath, true)
                if (path.name.contains("Regular"))
                    FONT_MINECRAFT = Font.createFont(Font.TRUETYPE_FONT, filePath.toFile())
                else if (path.name.contains("Bold") && !path.name.contains("Italic"))
                    FONT_MINECRAFT_BOLD = Font.createFont(Font.TRUETYPE_FONT, filePath.toFile())
                else if (path.name.contains("Italic") && !path.name.contains("Bold"))
                    FONT_MINECRAFT_ITALIC = Font.createFont(Font.TRUETYPE_FONT, filePath.toFile())
                else
                    Font.createFont(Font.TRUETYPE_FONT, filePath.toFile())
            }
        }
        Threading.runAsync {
            Discord.readSelf()
            TransactionHistory.readSelf()
            Calendar.readSelf()
        }
    }

    override fun startup() {

        // required to be sync
        Ability.init()
        ReforgeType.init()
        StatRune.init()
        ItemValue.init()
        Armor.init()
        Bazaar.init()

        Threading.runEachConcurrently(
            Executors.newFixedThreadPool(8),
            ScrollRecipe::init,
            Enchant::init,
            RecipeValue::init,
            Buffs::init,
            EntityValue::init,
            PetValue::init,
            ZoneType::init,
            Cosmetics::init,
            SlayerType::init,
            ItemParser::init,
            ForgeRecipe::initRecipes,
            SpellValue::initSpells,
            SeaCreatures::init,
            TrophyFishes::init,
            PyroclasticToadPet::init,
            WaspPet::init
        )

        Registry.BLOCK.register(id("my_test_block"), object : MacrocosmBlock {
            override val id: Identifier = id("my_test_block")
            override val hardness: Int = (MacrocosmBlock.HARDNESS_STONE * 1.5).roundToInt()
            override val steadiness: Int = MacrocosmBlock.STEADINESS_MIN
            override val suitableTools: List<ItemType> = ItemType.all()
            override val baseExperience: Pair<Float, SkillType> = 100f to SkillType.ALCHEMY

            override fun pool(player: Player, mc: MacrocosmPlayer): LootPool {
                return LootPool.of(vanilla(Material.DIAMOND, 1.0, amount = 1..5))
            }

            override fun clone(): Clone {
                return this
            }
        })

        DataListener.joinLeave()
        server.pluginManager.registerEvents(ChatHandler, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(AbilityTriggers, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(DamageHandlers, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(EntityHandlers, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(RecipeMenu, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(BlockClickListener, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(PickupListener, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(AlchemyReward, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(MiningHandler, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(DamageHandlers, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(FishingHandler, this@InternalMacrocosmPlugin)
        // server.pluginManager.registerEvents(EquipListener, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(FallingBlockListener, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(PackProvider, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(SidebarRenderer, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(SlayerHandlers, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(Calendar, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(ItemUpdateHandlers, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(EquipmentHandler, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(InventoryListeners, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(Discord.ConnectionLoop, this@InternalMacrocosmPlugin)
        server.pluginManager.registerEvents(CustomBlockHandlers, this@InternalMacrocosmPlugin)

        PACKET_MANAGER = ProtocolLibrary.getProtocolManager()
        protocolManager.addPacketListener(MiningHandler)

        playtimeCommand()
        rankCommand()
        statCommand()
        recombobulateCommand()
        reforgeCommand()
        myDamageCommand()
        itemCommand()
        giveCoinsCommand()
        enchantCommand()
        viewRecipeCommand()
        recipesCommand()
        addScrollCommand()
        setStarsCommand()
        addPotatoBooks()
        givePetItemCommand()
        addPetCommand()
        spawnPetCommand()
        itemsCommand()
        summonCommand()
        giveAdminItemCommand()
        armorCommand()
        cosmeticCommand()
        payCommand()
        setDateCommand()
        getSlayerRewardsCommand()
        openEquipmentCommand()
        addSpellCommand()
        apiCommand()
        essenceCommand()

        doTestEmitPost()
        giveRecipeCommand()
        testStatsCommand()
        testLevelUp()
        testCollUp()
        skillExp()
        collAmount()
        testCraftingTable()
        setSlayerLevelCommand()
        testSlayerCommand()
        testMaddoxMenuCommand()
        openForgeMenuCommand()
        infusionCommand()
        bazaarOpCommand()
        openBazaarMenuCommand()
        announceItemsCommand()
        petsCommand()
        placeBlockCommand()

        // registering resource generators
        Registry.RESOURCE_GENERATORS.register(id("pack_manifest"), generate("pack.mcmeta", PackDescription::descript))
        Registry.RESOURCE_GENERATORS.register(id("model_data"), CMDGenerator)
        Registry.RESOURCE_GENERATORS.register(id("model"), TexturedModelGenerator)
        Registry.RESOURCE_GENERATORS.register(id("mcmeta"), MetaGenerator)
        Registry.RESOURCE_GENERATORS.register(id("blocks"), HybridBlockModelGenerator)

        if (dumpTestData) {
            Threading.runAsync(isDaemon = true) {
                DataGenerators.registries()
            }
        }

        val cfgFile = dataFolder.resolve("config.yml")
        if (!cfgFile.exists()) {
            saveDefaultConfig()
            reloadConfig()
        }

        config.load(cfgFile)

        isSandbox = config.getBoolean("game.sandbox")

        MacrocosmConstants.DISCORD_BOT_TOKEN = config.getString("connections.discord-bot-token")
        if (MacrocosmConstants.DISCORD_BOT_TOKEN != null && MacrocosmConstants.DISCORD_BOT_TOKEN != "NULL") {
            connectDiscordCommand()
        }

        Threading.runAsync(runnable = Discord::setupBot)

        Calendar.init()
        SidebarRenderer.init()

        PackProvider.init()

        task(period = 60 * 10L, sync = false) {
            KeyManager.requests.clear()
        }

        taskRunLater(5 * 20L, sync = false) {
            // detect item registry changes
            if (Macrocosm.isOnline) {
                val previousVersion = SemanticVersion.fromString(
                    Accessor.access(".VERSION").let { if (it.exists()) it.readText() else null } ?: "0.0.0-null")
                if (this.version != previousVersion)
                    Discord.sendVersionDiff(previousVersion)
            }
        }
    }

    private val dumpTestData: Boolean = false

    override fun shutdown() {
        val storageExecutor = Threading.newFixedPool(16)

        storageExecutor.execute {
            for ((_, v) in loadedPlayers) {
                v.storeSelf(database)
            }
        }
        storageExecutor.execute {
            Calendar.save()
        }
        storageExecutor.execute {
            TRANSACTION_HISTORY.storeSelf()
        }
        storageExecutor.execute {
            Bazaar.table.storeSelf(database)
        }
        storageExecutor.execute { KeyManager.store() }
        storageExecutor.execute { Discord.storeSelf() }
        storageExecutor.execute { Accessor.overwrite(".VERSION") { os -> os.writeBytes(this.version.toString()) } }

        storageExecutor.shutdown()

        ZombieAbilities.doomCounter.iter { id ->
            worlds[0].getEntity(id)?.remove()
        }


        storageExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)

        ServerShutdownEvent().callEvent()
    }
}

val protocolManager by lazy { InternalMacrocosmPlugin.PACKET_MANAGER }
val Macrocosm by lazy { InternalMacrocosmPlugin.INSTANCE }
val database by lazy { InternalMacrocosmPlugin.DATABASE }
val logger by lazy { Macrocosm.logger }
val currentIp by lazy { MacrocosmConstants.CURRENT_IP }
val discordBotToken by lazy { MacrocosmConstants.DISCORD_BOT_TOKEN }

@UnsafeFeature
val unsafe by lazy { InternalMacrocosmPlugin.UNSAFE }
