@file:Suppress("UNCHECKED_CAST")

package space.maxus.macrocosm.registry

import net.axay.kspigot.extensions.pluginManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.cosmetic.Cosmetic
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.entity.EntitySoundBank
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.fishing.FishingTreasure
import space.maxus.macrocosm.fishing.SeaCreature
import space.maxus.macrocosm.fishing.TrophyFish
import space.maxus.macrocosm.generators.Model
import space.maxus.macrocosm.generators.CMDGenerator
import space.maxus.macrocosm.generators.ResGenerator
import space.maxus.macrocosm.generators.TexturedModelGenerator
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.pets.Pet
import space.maxus.macrocosm.recipes.MacrocosmRecipe
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.util.GSON_PRETTY
import space.maxus.macrocosm.util.id
import space.maxus.macrocosm.zone.Zone
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

abstract class Registry<T>(val name: Identifier) {
    val delegates: AtomicInteger = AtomicInteger(0)
    protected val logger: Logger = LoggerFactory.getLogger("$name")
    abstract fun iter(): ConcurrentHashMap<Identifier, T>
    abstract fun register(id: Identifier, value: T): T
    open fun byValue(value: T): Identifier? {
        return if (value is Identified) value.id else iter().filter { it.value == value }.map { it.key }.firstOrNull()
    }

    open fun find(id: Identifier): T = iter()[id]!!
    open fun findOrNull(id: Identifier): T? = iter()[id]
    open fun has(id: Identifier): Boolean = iter().containsKey(id)

    inline fun delegateRegistration(
        values: List<Pair<Identifier, T>>,
        crossinline delegate: (Identifier, T) -> Unit = { _, _ -> }
    ) {
        Threading.runAsync("$name Delegate #${delegates.incrementAndGet()}") {
            this.info("Starting '$name' registry Delegate ${delegates.get()}")
            val pool = Threading.newFixedPool(8)

            for ((id, value) in values) {
                pool.execute {
                    register(id, value)
                    delegate(id, value)
                }
            }

            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
            this.info("Successfully registered ${values.size} elements in $name delegate.")
            delegates.decrementAndGet()
        }
    }

    open fun dumpToFile(file: Path) {
        file.deleteIfExists()
        logger.info("Saving data on registry '$name'...")
        file.writeText(GSON_PRETTY.toJson(iter()))
    }

    companion object : DefaultedRegistry<Registry<*>>(id("global")) {
        private val registries: ConcurrentHashMap<Identifier, Registry<*>> = ConcurrentHashMap()
        fun <V> register(registry: Registry<V>, id: Identifier, value: V) = registry.register(id, value)
        fun <V> register(registry: Registry<V>, id: String, value: V) = registry.register(id(id), value)

        private fun <V> makeDefaulted(name: Identifier): Registry<V> =
            register(name, DefaultedRegistry<V>(name)) as Registry<V>

        private fun <V> makeCloseable(name: Identifier): CloseableRegistry<V> {
            val reg = CloseableRegistry<V>(name)
            reg.open()
            return register(name, reg) as CloseableRegistry<V>
        }

        private fun <V: Clone> makeImmutable(name: Identifier, delegate: DelegatedRegistry<V>.(Identifier, V) -> Unit = { _, _ -> })
            = register(name, ImmutableRegistry(name, delegate)) as Registry<V>

        private fun <V> makeDelegated(name: Identifier, delegate: DelegatedRegistry<V>.(Identifier, V) -> Unit) =
            register(name, DelegatedRegistry(name, delegate)) as Registry<V>

        val ITEM = makeImmutable<MacrocosmItem>(id("item"))
        val ABILITY = makeDelegated<MacrocosmAbility>(id("ability")) { _, v ->
            v.registerListeners()
        }
        val ENTITY = makeDelegated<MacrocosmEntity>(id("entity")) { _, v ->
            pluginManager.registerEvents(v, Macrocosm)
        }
        val DISGUISE = makeDefaulted<String>(id("entity_disguise"))
        val SOUND = makeDefaulted<EntitySoundBank>(id("entity_sound"))
        val ZONE = makeDefaulted<Zone>(id("zone"))
        val REFORGE = makeDelegated<Reforge>(id("reforge")) { _, v ->
            pluginManager.registerEvents(v, Macrocosm)
        }
        val ENCHANT = makeDelegated<Enchantment>(id("enchant")) { _, v ->
            pluginManager.registerEvents(v, Macrocosm)
        }
        val PET = makeDelegated<Pet>(id("pet")) { _, v ->
            pluginManager.registerEvents(v, Macrocosm)
        }
        val RECIPE = makeDefaulted<MacrocosmRecipe>(id("recipe"))
        val LOOT_POOL = makeDefaulted<LootPool>(id("loot_pool"))
        val SEA_CREATURE = makeDefaulted<SeaCreature>(id("sea_creature"))
        val TROPHY_FISH = makeDefaulted<TrophyFish>(id("trophy_fish"))
        val FISHING_TREASURE = makeDefaulted<FishingTreasure>(id("fishing_treasure"))
        val SLAYER = makeDelegated<Slayer>(id("slayer")) { _, slayer ->
            val registeredAbilities = mutableListOf<String>()
            var minisRegistered = 0
            for(tier in slayer.tiers) {
                ENTITY.register(id("${slayer.id}_$tier"), slayer.bossForTier(tier))
                for(ability in slayer.abilitiesForTier(tier)) {
                    if(registeredAbilities.contains(ability.abilityId))
                        continue
                    ability.listenerRegister(ability)
                    registeredAbilities.add(ability.abilityId)
                }
                for(mini in slayer.minisForTier(tier)) {
                    ENTITY.register(id("${slayer.id}_miniboss_$minisRegistered"), mini)
                    minisRegistered++
                }
            }
        }
        val COSMETIC = makeDefaulted<Cosmetic>(id("cosmetic"))
        val MODEL_PREDICATES = makeDelegated<Model>(id("model")) { _, model ->
            CMDGenerator.enqueue(model)
            if(model.to.startsWith("macrocosm:"))
                TexturedModelGenerator.enqueue(model)
        }

        val RESOURCE_GENERATORS = makeDefaulted<ResGenerator>(id("resource_gen"))

        override fun register(id: Identifier, value: Registry<*>): Registry<*> {
            val r = super.register(id, value)
            logger.info("Prepared registry '$id'")
            return r
        }

        override fun dumpToFile(file: Path) {

        }
    }
}
