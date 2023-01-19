package space.maxus.macrocosm.mongo

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo
import org.litote.kmongo.util.KMongoJacksonFeature
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.mongo.data.*
import java.util.concurrent.Executor

object MongoDb {
    val mongoPool: Executor = Threading.newFixedPool(8)

    private lateinit var db: MongoDatabase

    lateinit var players: MongoCollection<MongoPlayerData>; private set
    lateinit var bazaar: MongoCollection<MongoBazaarData>; private set
    lateinit var limitedItems: MongoCollection<MongoLimitedEditionItem>; private set
    lateinit var transactions: MongoCollection<MongoTransaction>; private set
    lateinit var discordAuth: MongoCollection<MongoDiscordAuthentication>; private set

    fun init() {
        mongoPool.execute {
            System.setProperty(
                "org.litote.mongo.test.mapping.service",
                "org.litote.kmongo.jackson.JacksonClassMappingTypeService"
            )
            KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)
            val client = KMongo.createClient(
                MongoClientSettings
                    .builder()
                    .credential(MongoCredential.createCredential("macrocosm", "macrocosm", "macrocosm".toCharArray()))
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

            Macrocosm.playersLazy = players.find().map { it.uuid }.toMutableList()
            Discord.readSelf()
        }
    }

    inline fun execute(crossinline executor: (db: MongoDb) -> Unit) = mongoPool.execute { executor(this) }
}
