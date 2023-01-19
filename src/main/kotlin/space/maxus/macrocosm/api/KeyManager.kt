package space.maxus.macrocosm.api

import io.ktor.server.application.*
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.mongo.MongoDb
import space.maxus.macrocosm.mongo.data.MongoKeyData
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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
     * Reads this key manager data from MongoDB
     */
    fun load() {
        owned = MongoDb.apiKeys.find().map { KeyData(it.key, it.data) }.toMutableList()
    }

    /**
     * Stores key data in MongoDB
     */
    fun store() {
        MongoDb.apiKeys.insertMany(owned.map { MongoKeyData(it.key, it.data) })
    }

    /**
     * Generates a random key with metadata in it
     *
     * @param belongs The UUID of a player this key belongs to
     * @param permissions Allowed permissions for this key
     * @return Generated key data
     */
    fun generateRandomKey(belongs: UUID, permissions: List<APIPermission>): String {
        val r = SecureRandom.getInstanceStrong()

        val buf = ByteBuffer.allocate(2 + Long.SIZE_BYTES * 3)
        // header
        buf.put("mx".encodeToByteArray())
        buf.putLong(r.nextLong() ushr 3)
        buf.putLong(r.nextLong() ushr 2)
        buf.putLong(r.nextLong() ushr 1)
        // previous key data was too generic, we have stored enough data in the inlined value

        val s = Base64.getUrlEncoder().encodeToString(buf.array()).trimEnd('=')
        val data = KeyData(s, InlinedKeyData(currentApiState, Instant.now(), permissions, belongs))
        owned.removeIf { it.data.owner == belongs }
        owned.add(data)
        return s
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
