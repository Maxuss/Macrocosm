package space.maxus.macrocosm

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import net.axay.kspigot.extensions.worlds
import net.axay.kspigot.main.KSpigot
import net.minecraft.server.MinecraftServer
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.commands.*
import space.maxus.macrocosm.cosmetic.Cosmetics
import space.maxus.macrocosm.data.DataGenerators
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.db.DatabaseAccess
import space.maxus.macrocosm.db.local.SqliteDatabaseImpl
import space.maxus.macrocosm.db.postgres.PostgresDatabaseImpl
import space.maxus.macrocosm.display.SidebarRenderer
import space.maxus.macrocosm.enchants.Enchant
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.events.ServerShutdownEvent
import space.maxus.macrocosm.fishing.FishingHandler
import space.maxus.macrocosm.fishing.SeaCreatures
import space.maxus.macrocosm.fishing.TrophyFishes
import space.maxus.macrocosm.forge.ForgeRecipe
import space.maxus.macrocosm.generators.CMDGenerator
import space.maxus.macrocosm.generators.MetaGenerator
import space.maxus.macrocosm.generators.TexturedModelGenerator
import space.maxus.macrocosm.generators.generate
import space.maxus.macrocosm.item.Armor
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.buffs.Buffs
import space.maxus.macrocosm.item.json.ItemParser
import space.maxus.macrocosm.item.runes.StatRune
import space.maxus.macrocosm.listeners.*
import space.maxus.macrocosm.mining.MiningHandler
import space.maxus.macrocosm.net.MacrocosmServer
import space.maxus.macrocosm.pack.PackDescription
import space.maxus.macrocosm.pack.PackProvider
import space.maxus.macrocosm.pets.PetValue
import space.maxus.macrocosm.pets.types.PyroclasticToadPet
import space.maxus.macrocosm.pets.types.WaspPet
import space.maxus.macrocosm.players.EquipmentHandler
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.recipes.RecipeMenu
import space.maxus.macrocosm.recipes.RecipeValue
import space.maxus.macrocosm.reforge.ReforgeType
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.AlchemyReward
import space.maxus.macrocosm.slayer.SlayerHandlers
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.zombie.ZombieAbilities
import space.maxus.macrocosm.spell.SpellValue
import space.maxus.macrocosm.util.Monitor
import space.maxus.macrocosm.util.annotations.UnsafeFeature
import space.maxus.macrocosm.util.data.Unsafe
import space.maxus.macrocosm.util.game.Calendar
import space.maxus.macrocosm.util.generic.id
import space.maxus.macrocosm.workarounds.AsyncLauncher
import space.maxus.macrocosm.zone.ZoneType
import java.util.*
import java.util.concurrent.Executors
import kotlin.random.Random

@OptIn(UnsafeFeature::class)
class InternalMacrocosmPlugin : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMacrocosmPlugin; private set
        lateinit var PACKET_MANAGER: ProtocolManager; private set
        lateinit var UNSAFE: Unsafe; private set
        lateinit var MONITOR: Monitor; private set
        lateinit var DATABASE: DatabaseAccess; private set
    }

    val constantProfileId: UUID = UUID.fromString("13e76730-de52-4197-909a-6d50e0a2203b")
    val id: String = "macrocosm"
    val onlinePlayers: HashMap<UUID, MacrocosmPlayer> = hashMapOf()
    var isInDevEnvironment: Boolean = false; private set
    lateinit var integratedServer: MacrocosmServer; private set
    lateinit var playersLazy: MutableList<UUID>; private set

    override fun load() {
        isInDevEnvironment = java.lang.Boolean.getBoolean("macrocosm.dev")
        integratedServer =
            MacrocosmServer((if (isInDevEnvironment) "devMini" else "mini") + Random.nextBytes(1)[0].toString(16))
        INSTANCE = this
        UNSAFE = Unsafe(Random.nextInt())
        MONITOR = Monitor()
        Accessor.init()
        Threading.runAsync {
            info("Starting REST API Server")
            Monitor.enter("REST API Server Thread")
            AsyncLauncher.launchApi()
            Monitor.exit()
        }
        Threading.runAsyncRaw {
            DATABASE =
                if (isInDevEnvironment) SqliteDatabaseImpl else PostgresDatabaseImpl(System.getProperty("macrocosm.postgres.remote"))
            DATABASE.connect()
            playersLazy = DATABASE.readPlayers().toMutableList()
        }
        Threading.runAsyncRaw {
            Calendar.load()
        }
    }

    override fun startup() {
        Monitor.inject(MinecraftServer.getServer().serverThread)
        Monitor.enter("Event Registration")

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

        PACKET_MANAGER = ProtocolLibrary.getProtocolManager()
        protocolManager.addPacketListener(MiningHandler)

        Monitor.exit()
        Monitor.enter("Registry Initialization")

        // required to be sync
        ReforgeType.init()
        ItemValue.init()
        Armor.init()

        Threading.runEachConcurrently(
            Executors.newFixedThreadPool(8),
            StatRune::init,
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

        Monitor.exit()
        Monitor.enter("Command Registration")

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

        Monitor.exit()
        Monitor.enter("Resource Generation")

        // registering resource generators
        Registry.RESOURCE_GENERATORS.register(id("pack_manifest"), generate("pack.mcmeta", PackDescription::descript))
        Registry.RESOURCE_GENERATORS.register(id("model_data"), CMDGenerator)
        Registry.RESOURCE_GENERATORS.register(id("model"), TexturedModelGenerator)
        Registry.RESOURCE_GENERATORS.register(id("mcmeta"), MetaGenerator)

        Monitor.exit()

        if (dumpTestData) {
            Threading.runAsyncRaw(isDaemon = true) {
                Monitor.enter("Data generation")
                DataGenerators.registries()
                Monitor.exit()
            }
        }

        val cfgFile = dataFolder.resolve("config.yml")
        if (!cfgFile.exists()) {
            Monitor.enter("Config")
            saveDefaultConfig()
            reloadConfig()
            Monitor.exit()
        }

        config.load(cfgFile)

        Calendar.init()
        SidebarRenderer.init()

        PackProvider.init()
    }

    private val dumpTestData: Boolean = false

    override fun shutdown() {
        Threading.runAsync {
            for ((id, v) in onlinePlayers) {
                println("Saving data for player $id...")
                v.storeSelf(database.statement)
            }
        }
        Threading.runAsync {
            Calendar.save()
        }
        ZombieAbilities.doomCounter.iter { id ->
            worlds[0].getEntity(id)?.remove()
        }
        ServerShutdownEvent().callEvent()
    }
}

val protocolManager by lazy { InternalMacrocosmPlugin.PACKET_MANAGER }
val Macrocosm by lazy { InternalMacrocosmPlugin.INSTANCE }
val database by lazy { InternalMacrocosmPlugin.DATABASE }
val monitor by lazy { InternalMacrocosmPlugin.MONITOR }
val logger by lazy { Macrocosm.logger }

@UnsafeFeature
val unsafe by lazy { InternalMacrocosmPlugin.UNSAFE }
