package space.maxus.macrocosm.players

import space.maxus.macrocosm.db.mongo.MongoConvert
import space.maxus.macrocosm.db.mongo.data.MongoPlayerMemory
import space.maxus.macrocosm.registry.Identifier
import java.io.Serializable

data class PlayerMemory(
    val tier6Slayers: MutableList<Identifier>,
    val knownPowers: MutableList<Identifier>
) : Serializable, MongoConvert<MongoPlayerMemory> {
    companion object {
        fun nullMemory() = PlayerMemory(
            mutableListOf(), mutableListOf(
                "fortuitous",
                "pretty",
                "protected",
                "simple",
                "warrior",
                "inspired",
                "ominous",
            ).map(Identifier::macro).toMutableList()
        )
    }

    override val mongo: MongoPlayerMemory
        get() = MongoPlayerMemory(tier6Slayers.map(Identifier::toString), knownPowers.map(Identifier::toString))
}
