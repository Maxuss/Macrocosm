package space.maxus.macrocosm.db.mongo.data

import org.bson.codecs.pojo.annotations.BsonId

data class MongoLimitedEditionItem(
    @BsonId
    val item: String,
    val count: Int
)
