package space.maxus.macrocosm.db.mongo

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import space.maxus.macrocosm.bazaar.BazaarBuyOrder
import space.maxus.macrocosm.bazaar.BazaarSellOrder
import space.maxus.macrocosm.db.mongo.data.MongoBazaarOrder
import space.maxus.macrocosm.util.general.id
import java.util.*

object MongoDb {
    private lateinit var db: MongoDatabase; private set
    fun init() {
        System.setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")
        val client = KMongo.createClient(MongoClientSettings.builder().credential(MongoCredential.createCredential("macrocosm", "macrocosm", "macrocosm".toCharArray())).build())
        val db = client.getDatabase("macrocosm")
        val col = db.getCollection("bazaarOrders", MongoBazaarOrder::class.java)
        col.insertMany(listOf(
            BazaarBuyOrder(id("raw_titanium"), 1213, 12515.12, 12, mutableListOf(UUID.randomUUID()), UUID.randomUUID(), 1225).mongo,
            BazaarSellOrder(id("raw_titanium"), 1213, 12515.12, 12, mutableListOf(UUID.randomUUID()), UUID.randomUUID(), 1225).mongo,
            ))
    }
}
