package space.maxus.macrocosm

import net.axay.kspigot.main.KSpigot
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.ChatHandler
import space.maxus.macrocosm.commands.*
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.listeners.AbilityTriggers
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.listeners.DataListener
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.ReforgeType
import java.util.*

class InternalMacrocosmPlugin : KSpigot() {
    companion object {
        lateinit var INSTANCE: InternalMacrocosmPlugin; private set
    }

    val onlinePlayers: HashMap<UUID, MacrocosmPlayer> = hashMapOf()
    lateinit var playersLazy: MutableList<UUID>; private set

    override fun load() {
        INSTANCE = this
        Threading.start {
            Database.connect()
            playersLazy = Database.readAllPlayers().toMutableList()
        }
    }

    override fun startup() {
        DataListener.joinLeave()
        server.pluginManager.registerEvents(ChatHandler, this)
        server.pluginManager.registerEvents(AbilityTriggers, this)
        server.pluginManager.registerEvents(DamageHandlers, this)

        ReforgeType.init()

        playtimeCommand()
        rankCommand()
        statCommand()
        recombobulateCommand()
        reforgeCommand()
        myDamageCommand()

        testItemCommand()
        testStatsCommand()
        testEntityCommand()
    }

    override fun shutdown() {
        Threading.start {
            for ((id, v) in onlinePlayers) {
                println("Saving data for player $id...")
                v.storeSelf(Database.statement)
            }
        }
    }
}

val Macrocosm by lazy { InternalMacrocosmPlugin.INSTANCE }
