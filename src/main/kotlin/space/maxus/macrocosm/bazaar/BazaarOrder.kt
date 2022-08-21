@file:Suppress("PropertyName")

package space.maxus.macrocosm.bazaar

import space.maxus.macrocosm.registry.Identifier
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class BazaarDataCompound(val orders: List<BazaarOrder>)

abstract class BazaarOrder(val item: Identifier, val createdBy: UUID, val createdAt: Long = Instant.now().toEpochMilli()) {
    abstract val totalPrice: BigDecimal
}

class BazaarBuyOrder(item: Identifier, var qty: Int, val pricePer: Double, var bought: Int, val sellers: MutableList<UUID>, createdBy: UUID, createdAt: Long = Instant.now().toEpochMilli()): BazaarOrder(item, createdBy, createdAt) {
    override val totalPrice: BigDecimal = pricePer.toBigDecimal() * qty.toBigDecimal()
}

class BazaarSellOrder(item: Identifier, var qty: Int, val pricePer: Double, var sold: Int, val buyers: MutableList<UUID>, createdBy: UUID, createdAt: Long = Instant.now().toEpochMilli()): BazaarOrder(item, createdBy, createdAt) {
    override val totalPrice: BigDecimal = pricePer.toBigDecimal() * qty.toBigDecimal()
}
