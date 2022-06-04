package space.maxus.macrocosm.display

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.DisplaySlot
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Ticker
import space.maxus.macrocosm.util.setOrAppend
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import kotlin.math.max

object SidebarRenderer: Listener {
    private val renderQueue: ConcurrentHashMap<UUID, ConcurrentLinkedQueue<RenderComponent>> = ConcurrentHashMap()
    private val renderPool: ExecutorService = Threading.newFixedPool(5)

    fun enqueue(player: Player, component: RenderComponent): Int {
        return renderQueue.setOrAppend(player.uniqueId, component)
    }

    fun dequeue(player: Player, component: RenderComponent) {
        val q = renderQueue[player.uniqueId]!!
        q.remove(component)
        renderQueue[player.uniqueId] = q
    }

    private val objNameMap = ChatColor.values().map { it.toString() + ChatColor.RESET.toString() }
    private val max = objNameMap.size
    private var ticker: Ticker = Ticker(0..10)
    fun tick() {
        for(player in Bukkit.getOnlinePlayers()) {
            val components = renderQueue[player.uniqueId] ?: continue
            val obj = player.scoreboard.getObjective("defaultBoard")!!
            val board = player.scoreboard
            var pos = -1
            for (cmp in components) {
                pos++
                if(pos > objNameMap.size - 1)
                    break
                val name = objNameMap[pos]
                val team = try { board.registerNewTeam(name) } catch (ignored: java.lang.IllegalArgumentException) { board.getTeam(name) }!!
                team.addEntry(name)
                team.prefix(cmp.title())
                obj.getScore(name).score = max - (pos + 2)
                val lines = cmp.lines().toMutableList()
                // padding
                lines.add("".toComponent())
                for(line in lines) {
                    pos++
                    val nn = objNameMap[pos]
                    val t = try { board.registerNewTeam(nn) } catch (ignored: java.lang.IllegalArgumentException) { board.getTeam(nn) }!!
                    t.prefix(line)
                    t.addEntry(nn)
                    obj.getScore(nn).score = max - (pos + 2)
                }
            }
        }
    }

    fun tickTitle() {
        ticker.tick()
        for(player in Bukkit.getOnlinePlayers()) {
            updateBoardTitle(player)
        }
    }

    fun init() {
        task(false, delay = 20L, period = 10L) {
            tick()
        }
        task(false, delay = 20L, period = 5L) {
            tickTitle()
        }
    }

    private fun updateBoardTitle(player: Player) {
        val obj = player.scoreboard.getObjective("defaultBoard")!!
        val tick = ticker.position()
        val startColor = TextColor.color(0xA866FF)
        val endColor = TextColor.color(0xFF6825)
        val name = StringBuilder("MACROCOSM")
        name.insert(max(tick - 1, 0), "<gradient:#F5A556:#957CF9>")
        val mmName = "<gradient:${startColor.asHexString()}:${endColor.asHexString()}><bold>"+" ".repeat(5) + name + " ".repeat(5)
        obj.displayName(comp(mmName))
    }

    private fun preparePlayer(player: Player) {
        val manager = Bukkit.getScoreboardManager()
        val board = manager.newScoreboard
        val obj = board.registerNewObjective("defaultBoard", "dummy", comp("<light_purple><bold>MACROCOSM"))
        obj.displaySlot = DisplaySlot.SIDEBAR

        val sc = obj.getScore(ChatColor.DARK_GRAY.toString() + "Current Server: ${Macrocosm.integratedServer.id}")
        sc.score = max
        val spacing = obj.getScore(ChatColor.BLACK.toString())
        spacing.score = max - 1

        player.scoreboard = board
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        preparePlayer(e.player)

        enqueue(e.player, RenderComponent.simple(
            "<red><bold>TEST STUFF",
            "<gray>Im just testing stuff asdasdaasd"
        ))
        enqueue(e.player, RenderComponent.fixed(
            comp("<gradient:red:dark_purple><bold>Another test"),
            listOf(comp("<gray>First line"), comp("<gray>Second line"))
        ))
    }

    @EventHandler
    fun onLeave(e: PlayerQuitEvent) {
        renderQueue.remove(e.player.uniqueId)
    }
}
