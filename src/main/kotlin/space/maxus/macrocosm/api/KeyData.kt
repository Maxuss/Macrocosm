package space.maxus.macrocosm.api

/**
 * Macrocosm API Access Key data
 *
 * @property key The key string
 * @property data Data stored in the key
 */
data class KeyData(val key: String, val data: InlinedKeyData)

/**
 * Represents byte-inlined data stored in the key as base64
 *
 * @property createdAt Unix epoch timestamp of when the key was created
 * @property keyIdentifierUnique Unique random number associated with this key
 * @property permissions Permissions allowed for this key
 * @property ownerMostBits Most significant bits of owner's UUID
 */
data class InlinedKeyData(val format: APIState, val createdAt: Long, val keyIdentifierUnique: Int, val permissions: List<APIPermission>, val ownerMostBits: Long)

/**
 * Type of api state
 *
 * @property message Type of message sent on invalid key provided with following state if the global API state is different
 */
enum class APIState(val message: String) {
    INDEV("Key is using an outdated indev format"),
    PROD("Key is using release format which might not be supported in beta!")
}

/**
 * Type of permission for an API Key
 */
enum class APIPermission {
    /**
     * The player can access the `/players` endpoints
     */
    VIEW_PLAYER_DATA,

    /**
     * The player can access the `/bazaar` endpoints
     */
    VIEW_BAZAAR_DATA
}
