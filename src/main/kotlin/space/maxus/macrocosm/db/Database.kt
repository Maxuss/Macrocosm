package space.maxus.macrocosm.db

import space.maxus.macrocosm.database
import space.maxus.macrocosm.logger
import space.maxus.macrocosm.registry.Identifier
import java.util.*

@org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
@Deprecated(
    "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
    ReplaceWith("space.maxus.macrocosm.database")
)
object Database {
    @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
    @Deprecated(
        "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
        ReplaceWith("space.maxus.macrocosm.database.connect()")
    )
    fun connect() {
        logger.warning("Using deprecated method Database#connect! Use DatabaseAccess instead!")
        database.connect()
    }

    @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
    @Deprecated(
        "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
        ReplaceWith("space.maxus.macrocosm.database.readAllPlayers()")
    )
    fun readAllPlayers(): List<UUID> {
        logger.warning("Using deprecated method Database#readAllPlayers! Use DatabaseAccess instead!")
        return database.readPlayers()
    }

    @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
    @Deprecated("Functionality was merged with DatabaseAccess", ReplaceWith("space.maxus.macrocosm.database"))
    object Limited {
        @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval
        @Deprecated(
            "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
            ReplaceWith("space.maxus.macrocosm.database.incrementLimitedEdition(item)")
        )
        fun incrementGet(item: Identifier): Int {
            logger.warning("Using deprecated method Limited#incrementGet! Use DatabaseAccess instead!")
            return database.incrementLimitedEdition(item)
        }
    }
}
