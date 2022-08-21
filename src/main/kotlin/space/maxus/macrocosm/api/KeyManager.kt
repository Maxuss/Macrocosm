package space.maxus.macrocosm.api

import com.google.gson.reflect.TypeToken
import io.ktor.server.application.*
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.util.GSON
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.readText
import kotlin.io.path.writeText

object KeyManager {
    private var owned: MutableList<KeyData> = mutableListOf()
    val requests: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    fun load() {
        try {
            owned = GSON.fromJson(Accessor.access("keys.json").readText(), object: TypeToken<HashMap<UUID, String>>() { }.type)
        } catch(ignored: Exception) {
            // first time access, don't care
        }
    }

    fun store() {
        Accessor.access("keys.json").writeText(GSON.toJson(owned))
    }

    fun generateRandomKey(belongs: UUID, permissions: List<APIPermission>): String {
        val r = ThreadLocalRandom.current()
        val buf = ByteBuffer.allocate(36)

        val bytes1 = ByteArray(12)
        r.nextBytes(bytes1)
        buf.put(bytes1)

        buf.putInt((belongs.leastSignificantBits shr 7).toInt())

        val bytes2 = ByteArray(12)
        r.nextBytes(bytes2)
        buf.put(bytes2)
        val s = Base64.getUrlEncoder().encodeToString(buf.array()).replace("_", "h").replace("=", "f").replace("-", "b").replace("AAAA", "")
        owned.removeIf { it.owner == belongs }
        owned.add(KeyData(belongs, s, permissions))
        return s
    }

    fun ApplicationCall.validateKey(requiredPermission: APIPermission): ValidationResult {
        val headers = this.request.headers
        val key = headers["API-Key"] ?: this.request.queryParameters["key"] ?: return ValidationResult.NO_KEY_PROVIDED
        if(owned.any { it.key == key }) {
            if(!this@KeyManager.requests.containsKey(key))
                this@KeyManager.requests[key] = 0
            if(this@KeyManager.requests[key]!! < 100) {
                val k = owned.first { it.key == key }
                if(!k.permissions.contains(requiredPermission))
                    return ValidationResult.INSUFFICIENT_PERMISSIONS
                this@KeyManager.requests[key] = this@KeyManager.requests[key]!! + 1
                return ValidationResult.SUCCESS
            }
            return ValidationResult.KEY_THROTTLE
        }
        return ValidationResult.INVALID_KEY
    }

    enum class ValidationResult {
        SUCCESS,
        NO_KEY_PROVIDED,
        INVALID_KEY,
        KEY_THROTTLE,
        INSUFFICIENT_PERMISSIONS
        ;

        operator fun not(): Boolean {
            return this != SUCCESS
        }
    }
}
