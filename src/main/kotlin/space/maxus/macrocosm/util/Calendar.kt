package space.maxus.macrocosm.util

import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import space.maxus.macrocosm.events.SeasonChangeEvent
import space.maxus.macrocosm.events.YearChangeEvent
import space.maxus.macrocosm.text.comp
import kotlin.io.path.Path
import kotlin.io.path.exists

object Calendar {
    // 0/0/0
    const val EPOCH: Long = 0

    private lateinit var tickTask: KSpigotRunnable

    var ticksSinceDateChange: Long = 0L
    var date: Int = 1
    var season: Season = Season.SPRING
    var state: SeasonState = SeasonState.EARLY
    var year: Int = 1

    enum class SeasonState(val display: String) {
        EARLY("Early"),
        MID(""),
        LATE("Late")

        ;

        fun next(): SeasonState {
            return when(this) {
                EARLY -> MID
                MID -> LATE
                LATE -> EARLY
            }
        }
    }

    enum class Season(val display: String) {
        SUMMER("<yellow><state> Summer ♫"),
        FALL("<gold><state> Fall ☔"),
        WINTER("<aqua><state> Winter ❄"),
        SPRING("<green><state> Spring ⭐")

        ;

        fun next(): Season {
            return when(this) {
                SUMMER -> FALL
                FALL -> WINTER
                WINTER -> SPRING
                SPRING -> SUMMER
            }
        }
    }

    fun renderDate(): Component {
        val mm = MiniMessage.miniMessage()
        val strDate = date.toString()
        return mm.deserialize(season.display, Placeholder.parsed("state", state.display)).append(comp(" $strDate${if(strDate == "1") "st" else if(strDate.endsWith("2") || strDate.endsWith("3")) "nd" else "th"}"))
    }

    fun tick() {
        ticksSinceDateChange++
        if(ticksSinceDateChange >= 4800) {
            // next day
            ticksSinceDateChange = 0
            date++
            if(date == 7 || date == 14 || date > 21) {
                // next state
                state = state.next()
                if(state == SeasonState.EARLY) {
                    // next season
                    val new = season.next()
                    val event = SeasonChangeEvent(season, new)
                    event.callEvent()
                    season = new

                    if(season == Season.SPRING) {
                        // next year
                        val yEvent = YearChangeEvent(year + 1, year)
                        yEvent.callEvent()
                        year += 1
                    }
                }
            }

            if(date >= 21)
                date = 1
        }
    }

    fun init() {
        tickTask = task(false, delay = 5L) {
            tick()
        }!!
    }

    fun save() {
        tickTask.cancel()
        val file = Path(System.getProperty("user.dir"), "macrocosm", "calendar.dat")
        val stream = file.toFile().outputStream()
        val data = CompoundTag()
        data.putInt("Date", date)
        data.putInt("Season", season.ordinal)
        data.putInt("State", state.ordinal)
        data.putInt("Year", year)

        NbtIo.writeCompressed(data, stream)
    }

    fun load() {
        val file = Path(System.getProperty("user.dir"), "macrocosm", "calendar.dat")
        if(!file.exists())
            return
        val data = NbtIo.readCompressed(file.toFile())
        date = data.getInt("Date")
        season = Season.values()[data.getInt("Season")]
        state = SeasonState.values()[data.getInt("State")]
        year = data.getInt("Year")
    }
}
