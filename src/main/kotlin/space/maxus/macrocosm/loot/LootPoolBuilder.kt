package space.maxus.macrocosm.loot

import kotlin.math.roundToInt

class LootPoolBuilder(private var drops: MutableList<Drop> = mutableListOf()) {
    fun multiplyAmount(by: Float): LootPoolBuilder {
        drops = drops.map {
            val clone = it.clone()
            clone.amount = (it.amount.first * by).roundToInt()..(it.amount.last * by).roundToInt()
            clone
        }.toMutableList()
        return this
    }

    fun add(vararg drops: Drop): LootPoolBuilder {
        this.drops.addAll(drops)
        return this
    }

    fun build() = LootPool.of(*drops.toTypedArray())
}
