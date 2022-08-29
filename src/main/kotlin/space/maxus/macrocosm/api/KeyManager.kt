package space.maxus.macrocosm.api

import com.google.gson.reflect.TypeToken
import io.ktor.server.application.*
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.aggregate
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Represents current API state
 */
val currentApiState by lazy { if (Macrocosm.isInDevEnvironment) APIState.INDEV else APIState.PROD }

/**
 * A global key manage for operations with API Keys
 */
object KeyManager {
    private var owned: MutableList<KeyData> = mutableListOf()

    /**
     * Amount of requests per-key
     */
    val requests: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    /**
     * Reads this key manager data from the local file
     */
    fun load() {
        try {
            owned = GSON.fromJson(
                Accessor.access("keys.json").readText(),
                object : TypeToken<HashMap<UUID, String>>() {}.type
            )
        } catch (ignored: Exception) {
            // first time access, don't care
        }
    }

    /**
     * Stores key data in the local file, "keys.json" by default
     */
    fun store() {
        Accessor.access("keys.json").writeText(GSON.toJson(owned))
    }

    /**
     * Generates a random key with metadata in it
     *
     * @param belongs The UUID of a player this key belongs to
     * @param permissions Allowed permissions for this key
     * @return Generated key data
     */
    fun generateRandomKey(belongs: UUID, permissions: List<APIPermission>): String {
        val r = ThreadLocalRandom.current()
        val currentState = currentApiState.ordinal
        val now = Instant.now().toEpochMilli()
        val uniqueIdentifier = r.nextInt()
        val mostSignificantBits = belongs.mostSignificantBits

        val buf = ByteBuffer.allocate(1 + Long.SIZE_BYTES + Int.SIZE_BYTES + (1 + permissions.size) + Long.SIZE_BYTES)
        buf.put(currentState.toByte())
        buf.putLong(now)
        buf.putInt(uniqueIdentifier)
        buf.put(permissions.size.toByte())
        permissions.forEach {
            buf.put(it.ordinal.toByte())
        }
        buf.putLong(mostSignificantBits)

        val s = Base64.getUrlEncoder().encodeToString(buf.array())
        val data = KeyData(s, InlinedKeyData(currentApiState, now, uniqueIdentifier, permissions, mostSignificantBits))
        owned.add(data)
        return s
    }

    private fun readKeyData(key: String): InlinedKeyData? {
        return try {
            val buf = ByteBuffer.wrap(Base64.getUrlDecoder().decode(key))
            val format = APIState.values()[buf.get().toInt()]
            val createdAt = buf.long
            val uniqueIdentifier = buf.int
            val permissions = aggregate(buf.get().toInt()) { APIPermission.values()[buf.get().toInt()] }
            val mostSignificantBits = buf.long
            InlinedKeyData(format, createdAt, uniqueIdentifier, permissions, mostSignificantBits)
        } catch (e: Exception) {
            // invalid/legacy key format
            null
        }
    }

    /**
     * Validates key provided in the call request either in the `API-Key` header or in the query parameters
     *
     * @param requiredPermission Permission to be checked for
     * @return Result of key validation
     */
    fun ApplicationCall.validateKey(requiredPermission: APIPermission): ValidationResult {
        val headers = this.request.headers
        val key = headers["API-Key"] ?: this.request.queryParameters["key"] ?: return ValidationResult.NO_KEY_PROVIDED
        if (owned.any { it.key == key }) {
            if (!this@KeyManager.requests.containsKey(key))
                this@KeyManager.requests[key] = 0
            if (this@KeyManager.requests[key]!! < 100) {
                val k = owned.first { it.key == key }
                if (!k.data.permissions.contains(requiredPermission))
                    return ValidationResult.INSUFFICIENT_PERMISSIONS
                else if (k.data.format != currentApiState) {
                    return ValidationResult.INVALID_FORMAT
                }
                this@KeyManager.requests[key] = this@KeyManager.requests[key]!! + 1
                return ValidationResult.SUCCESS
            }
            return ValidationResult.KEY_THROTTLE
        }
        return ValidationResult.INVALID_KEY
    }

    /**
     * Result of key validation
     */
    enum class ValidationResult {
        /**
         * Validation successful
         */
        SUCCESS,

        /**
         * Validation failed due to the lack of key in request
         *
         */
        NO_KEY_PROVIDED,

        /**
         * Validation failed due to the key being invalid/of unreadable format
         *
         */
        INVALID_KEY,

        /**
         * Validation passed but the key is in the throttle limit for this minute due to the amount of requests
         *
         */
        KEY_THROTTLE,

        /**
         * Validation passed but the key does not conform required permission
         *
         */
        INSUFFICIENT_PERMISSIONS,

        /**
         * Validation passed but the key used an invalid format
         *
         */
        INVALID_FORMAT
        ;

        operator fun not(): Boolean {
            return this != SUCCESS
        }
    }
}
