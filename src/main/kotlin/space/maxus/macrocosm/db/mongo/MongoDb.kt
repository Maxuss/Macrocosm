package space.maxus.macrocosm.db.mongo

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.UuidRepresentation
import org.litote.kmongo.KMongo
import org.litote.kmongo.util.KMongoJacksonFeature
import space.maxus.macrocosm.db.mongo.data.MongoPlayerData

object MongoDb {
    private lateinit var db: MongoDatabase

    lateinit var players: MongoCollection<MongoPlayerData>; private set

    fun init() {
        System.setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")
        KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)
        val client = KMongo.createClient(
            MongoClientSettings
                .builder()
                .credential(MongoCredential.createCredential("macrocosm", "macrocosm", "macrocosm".toCharArray()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build())
        val db = client.getDatabase("macrocosm")
        this.db = db
        this.players = db.getCollection("players", MongoPlayerData::class.java)
    }
}
