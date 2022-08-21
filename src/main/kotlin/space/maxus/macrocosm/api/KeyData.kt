package space.maxus.macrocosm.api

import java.util.*

data class KeyData(val owner: UUID, val key: String, val permissions: List<APIPermission>)

enum class APIPermission {
    VIEW_PLAYER_DATA,
    VIEW_BAZAAR_DATA
}
