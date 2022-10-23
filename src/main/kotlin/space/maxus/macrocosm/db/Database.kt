package space.maxus.macrocosm.db

import space.maxus.macrocosm.database
import space.maxus.macrocosm.logger
import space.maxus.macrocosm.registry.Identifier
import java.util.*

/**
 * A legacy object that was used to access database
 */
@org.jetbrains.annotations.ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
@Deprecated(
    "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
    ReplaceWith("space.maxus.macrocosm.database")
)
object Database {
    /**
     * A legacy method that was used to connect to database
     */
    @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
    @Deprecated(
        "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
        ReplaceWith("space.maxus.macrocosm.database.connect()")
    )
    fun connect() {
        logger.warning("Using deprecated method Database#connect! Use DatabaseAccess instead!")
        database.connect()
    }

    /**
     * A legacy method that was used to read all player data
     */
    @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
    @Deprecated(
        "Use DatabaseAccess via `space.maxus.macrocosm.database` instead",
        ReplaceWith("space.maxus.macrocosm.database.readAllPlayers()")
    )
    fun readAllPlayers(): List<UUID> {
        logger.warning("Using deprecated method Database#readAllPlayers! Use DatabaseAccess instead!")
        return database.readPlayers()
    }

    /**
     * A legacy object that was used to access limited edition item data
     */
    @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
    @Deprecated("Functionality was merged with DatabaseAccess", ReplaceWith("space.maxus.macrocosm.database"))
    object Limited {
        /**
         * A legacy method that was used to increment the edition of item
         */
        @org.jetbrains.annotations.ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
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
