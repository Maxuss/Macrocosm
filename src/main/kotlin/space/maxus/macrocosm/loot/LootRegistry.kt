package space.maxus.macrocosm.loot

import space.maxus.macrocosm.util.Identifier

object LootRegistry {
    private val loot: HashMap<Identifier, LootPool> = hashMapOf()

    fun register(id: Identifier, loot: LootPool): LootPool {
        this.loot[id] = loot
        return loot
    }

    fun find(id: Identifier): LootPool = loot[id]!!
    fun findOrNull(id: Identifier): LootPool? = loot[id]
    fun nameOrNull(pool: LootPool) = loot.filter { it.value == pool }.keys.firstOrNull()
}
