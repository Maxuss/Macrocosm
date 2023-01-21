package space.maxus.macrocosm.mongo

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo
import org.litote.kmongo.util.KMongoJacksonFeature
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.api.KeyManager
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.mongo.data.*
import java.util.concurrent.Executor

/**
 * Instance controlling access to MongoDB
 */
object MongoDb {
    /**
     * Executor pool for MongoDB operations
     */
    val mongoPool: Executor = Threading.newFixedPool(8)

    private lateinit var db: MongoDatabase
    private var enabled: Boolean = false

    /**
     * The `players` collection
     */
    lateinit var players: MongoCollection<MongoPlayerData>; private set

    /**
     * The `bazaar` collection
     */
    lateinit var bazaar: MongoCollection<MongoBazaarData>; private set

    /**
     * The `limitedEdition` collection
     */
    lateinit var limitedItems: MongoCollection<MongoLimitedEditionItem>; private set

    /**
     * The `transactions` collection
     */
    lateinit var transactions: MongoCollection<MongoTransaction>; private set

    /**
     * The `discord` collection
     */
    lateinit var discordAuth: MongoCollection<MongoDiscordAuthentication>; private set

    /**
     * The `apiKeys` collection
     */
    lateinit var apiKeys: MongoCollection<MongoKeyData>; private set

    /**
     * Initializes MongoDB
     */
    fun init() {
        mongoPool.execute {
            try {
                System.setProperty(
                    "org.litote.mongo.test.mapping.service",
                    "org.litote.kmongo.jackson.JacksonClassMappingTypeService"
                )
                KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)
                val client = KMongo.createClient(
                    MongoClientSettings
                        .builder()
                        .credential(
                            MongoCredential.createCredential(
                                System.getProperty("mongo.user"),
                                "macrocosm",
                                System.getProperty("mongo.pass").toCharArray()
                            )
                        )
                        .uuidRepresentation(UuidRepresentation.STANDARD)
                        .build()
                )
                val db = client.getDatabase("macrocosm")
                MongoDb.db = db
                players = db.getCollection("players", MongoPlayerData::class.java)
                bazaar = db.getCollection("bazaar", MongoBazaarData::class.java)
                limitedItems = db.getCollection("limitedEdition", MongoLimitedEditionItem::class.java)
                transactions = db.getCollection("transactions", MongoTransaction::class.java)
                discordAuth = db.getCollection("discord", MongoDiscordAuthentication::class.java)
                apiKeys = db.getCollection("apiKeys", MongoKeyData::class.java)

                enabled = true

                Macrocosm.playersLazy = players.find().map { it.uuid }.toMutableList()
                Discord.readSelf()
                KeyManager.load()
            } catch (e: Exception) {
                Macrocosm.logger.severe("An error occurred when initializing MongoDB (${e.message}). Disabling it...")
                enabled = false
            }
        }
    }

    /**
     * Executes an operation with MongoDB thread executor if MongoDB is enabled
     */
    fun execute(executor: (db: MongoDb) -> Unit) = if (enabled) mongoPool.execute { executor(this) } else Unit
}
