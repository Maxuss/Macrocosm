package space.maxus.macrocosm.mongo.data

import org.bson.codecs.pojo.annotations.BsonId
import space.maxus.macrocosm.api.InlinedKeyData
import space.maxus.macrocosm.players.banking.Transaction
import java.time.Instant
import java.util.*

data class MongoLimitedEditionItem(
    @BsonId
    val item: String,
    val count: Int
)

data class MongoTransaction(
    @BsonId
    val id: UUID,
    val player: UUID,
    val kind: Transaction.Kind,
    val at: Instant,
    val amount: Double // we assume that no one will be sending over 2^64 coins. That's crazy
)

data class MongoDiscordAuthentication(
    @BsonId
    val playerId: UUID,
    val discordUID: Long
)

data class MongoKeyData(
    @BsonId
    val key: String,
    val data: InlinedKeyData
)
