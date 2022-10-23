package space.maxus.macrocosm.api

import space.maxus.macrocosm.bazaar.BazaarBuyOrder
import space.maxus.macrocosm.bazaar.BazaarSellOrder
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.collections.PlayerCollection
import space.maxus.macrocosm.ranks.Rank
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.skills.PlayerSkill
import space.maxus.macrocosm.skills.SkillType
import java.math.BigDecimal
import java.util.*

internal data class Success<T>(val success: Boolean = true, val value: T)
internal data class Failure(val success: Boolean = false, val error: Any)

internal data class Status(
    val status: String,
    val inDevEnv: Boolean,
    val macrocosmVersion: String,
    val apiVersion: String
)

internal data class AvailableRegistries(val available: Set<Identifier>)
internal data class GetRegistry<T>(val registry: Map<Identifier, T>)
internal data class GetRegistryElement<T>(val element: T)

internal data class OnlinePlayers(val onlinePlayers: Map<String, UUID>)
internal data class GeneralPlayerData(val rank: Rank, val firstJoin: Long, val lastJoin: Long, val playtime: Long)
internal data class PlayerStatus(val foundPlayer: Boolean, val uuid: UUID, val isOnline: Boolean)
internal data class PlayerInventory(val inventoryData: String)
internal data class PlayerBalance(val bank: BigDecimal, val purse: BigDecimal)
internal data class PlayerSkills(
    val skills: Map<SkillType, PlayerSkill>,
    val collections: Map<CollectionType, PlayerCollection>
)

internal data class BazaarStatus(val totalEntries: Int, val totalOrders: Int)
internal data class BazaarElements(val items: List<Identifier>)
internal data class ItemOrders(val buy: List<BazaarBuyOrder>, val sell: List<BazaarSellOrder>)
