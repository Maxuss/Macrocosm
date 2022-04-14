package space.maxus.macrocosm

import net.axay.kspigot.main.KSpigot
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.commands.playtimeCommand
import space.maxus.macrocosm.commands.rankCommand
import space.maxus.macrocosm.commands.statCommand
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.listeners.DataListener
import space.maxus.macrocosm.players.MacrocosmPlayer
import java.util.*
import kotlin.collections.HashMap

class InternalMacrocosmPlugin: KSpigot() {
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
        playtimeCommand()
        rankCommand()
        statCommand()
    }

    override fun shutdown() {
        Threading.start {
            for((_, v) in onlinePlayers) {
                v.storeSelf(Database.statement)
            }
        }
    }
}

val Macrocosm by lazy { InternalMacrocosmPlugin.INSTANCE }
