package space.maxus.macrocosm.display

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.worlds
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.game.Calendar
import space.maxus.macrocosm.util.general.Ticker
import space.maxus.macrocosm.util.ticksToTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import kotlin.math.max

/**
 * A renderer for a sidebar
 */
object SidebarRenderer : Listener {
    private val renderQueue: ConcurrentHashMap<UUID, ConcurrentHashMap<UUID, OrderedRenderComponent>> =
        ConcurrentHashMap()
    private val renderPool: ExecutorService = Threading.newFixedPool(5)

    /**
     * Enqueues a component to the rendering queue
     */
    fun enqueue(player: Player, component: RenderComponent, priority: RenderPriority): UUID {
        val cid = UUID.randomUUID()
        if (renderQueue.containsKey(player.uniqueId)) {
            renderQueue[player.uniqueId]!![cid] = OrderedRenderComponent(component, priority)
        } else {
            renderQueue[player.uniqueId] = ConcurrentHashMap<UUID, OrderedRenderComponent>().apply {
                put(
                    cid,
                    OrderedRenderComponent(component, priority)
                )
            }
        }
        return cid
    }

    /**
     * Dequeues a component with the [key] ID from the render queue
     */
    fun dequeue(player: Player, key: UUID) {
        val q = renderQueue[player.uniqueId]!!
        q.remove(key)
        renderQueue[player.uniqueId] = q
    }

    private val objNameMap = ChatColor.values().map { it.toString() + ChatColor.RESET.toString() }
    private val max = objNameMap.size
    private var ticker: Ticker = Ticker(0..10)

    /**
     * Performs a single render tick, rendering all components currently in queue
     *
     * @see enqueue
     * @see dequeue
     */
    fun tick() {
        for (player in Bukkit.getOnlinePlayers()) {
            val components = renderQueue[player.uniqueId] ?: continue
            val obj = player.scoreboard.getObjective("defaultBoard")!!
            val board = player.scoreboard
            var pos = -1
            val last = components.size - 1
            for ((index, data) in components.toList().sortedBy { (_, v) -> v.position.priority }.withIndex()) {
                val cmp = data.second
                pos++
                if (pos > objNameMap.size - 1)
                    break
                val name = objNameMap[pos]
                val team = try {
                    board.registerNewTeam(name)
                } catch (ignored: java.lang.IllegalArgumentException) {
                    board.getTeam(name)
                }!!
                team.addEntry(name)
                team.prefix(cmp.title())
                obj.getScore(name).score = max - (pos + 2)
                val lines = cmp.lines().toMutableList()

                // padding
                lines.add("".toComponent())
                if(index == last) {
                    lines.add(text("<yellow>play.maxus.space"))
                }
                for (line in lines) {
                    pos++
                    val nn = objNameMap[pos]
                    val t = try {
                        board.registerNewTeam(nn)
                    } catch (ignored: java.lang.IllegalArgumentException) {
                        board.getTeam(nn)
                    }!!
                    t.prefix(line)
                    t.addEntry(nn)
                    obj.getScore(nn).score = max - (pos + 2)
                }
            }
        }
    }

    private fun tickTitle() {
        ticker.tick()
        for (player in Bukkit.getOnlinePlayers()) {
            updateBoardTitle(player)
        }
    }

    /**
     * Initializes a sidebar renderer.
     *
     * This is a **Thread Safe** method
     */
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
        val mmName =
            "<gradient:${startColor.asHexString()}:${endColor.asHexString()}><bold>" + " ".repeat(5) + name + " ".repeat(
                5
            )
        obj.displayName(text(mmName))
    }

    private fun preparePlayer(player: Player) {
        val manager = Bukkit.getScoreboardManager()
        val board = manager.newScoreboard
        val obj = board.registerNewObjective("defaultBoard", Criteria.DUMMY, text("<light_purple><bold>MACROCOSM"))
        obj.displaySlot = DisplaySlot.SIDEBAR

        val sc = obj.getScore(ChatColor.DARK_GRAY.toString() + "Server: ${Macrocosm.integratedServer.id}")
        sc.score = max
        val spacing = obj.getScore(ChatColor.BLACK.toString())
        spacing.score = max - 1

        player.scoreboard = board
    }

    private var cachedDayTime = text("<dark_aqua>☽ <gray>00:00")
    private fun calculateDayTime(): Component {
        val world = worlds[0]
        val time = world.time
        val minute = (1000L / 60L) * 10
        // only update time every few minutes or so in game
        if (time % minute > 4)
            return cachedDayTime
        cachedDayTime = text(ticksToTime(time) + (if (world.isDayTime) "<gold>☀" else "<dark_aqua>☽") + "<gray> ")
        return cachedDayTime
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        preparePlayer(e.player)


        enqueue(
            e.player,
            RenderComponent.dynamic(Calendar::renderDate) { listOf(
                calculateDayTime(),
                text(" <gray>⏣ ${e.player.macrocosm?.zone?.name}")
                ) },
            RenderPriority.HIGHEST
        )

        enqueue(
            e.player,
            RenderComponent.dynamic({
                val mc = e.player.macrocosm ?: return@dynamic text("<red><bold>ERROR!")
                text("Purse: <gold>${Formatting.withCommas(mc.purse)}")
            }) { listOf() },
            RenderPriority.HIGHER
        )
    }

    @EventHandler
    fun onLeave(e: PlayerQuitEvent) {
        renderQueue.remove(e.player.uniqueId)
    }
}
