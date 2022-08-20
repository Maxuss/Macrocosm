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
import space.maxus.macrocosm.entity.EntityBase
import space.maxus.macrocosm.entity.EntitySoundBank
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.fishing.FishingTreasure
import space.maxus.macrocosm.fishing.SeaCreature
import space.maxus.macrocosm.fishing.TrophyFish
import space.maxus.macrocosm.forge.ForgeRecipe
import space.maxus.macrocosm.generators.*
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.pets.Pet
import space.maxus.macrocosm.recipes.MacrocosmRecipe
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.spell.Spell
import space.maxus.macrocosm.spell.essence.ScrollRecipe
import space.maxus.macrocosm.util.GSON_PRETTY
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.zone.Zone
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

abstract class Registry<T>(val name: Identifier, val shouldBeExposed: Boolean = true) {
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

            pool.shutdown()
            this.info("Successfully registered ${values.size} elements in $name delegate.")
            delegates.decrementAndGet()
        }
    }

    open fun dumpToFile(file: Path) {
        file.deleteIfExists()
        logger.info("Saving data on registry '$name'...")
        file.writeText(GSON_PRETTY.toJson(iter()))
    }

    companion object : DefaultedRegistry<Registry<*>>(id("global"), false) {
        private val registries: ConcurrentHashMap<Identifier, Registry<*>> = ConcurrentHashMap()
        fun <V> register(registry: Registry<V>, id: Identifier, value: V) = registry.register(id, value)
        fun <V> register(registry: Registry<V>, id: String, value: V) = registry.register(id(id), value)

        private fun <V> makeDefaulted(name: Identifier, expose: Boolean = true): Registry<V> =
            register(name, DefaultedRegistry<V>(name, expose)) as Registry<V>

        private fun <V> makeCloseable(name: Identifier, expose: Boolean = true): CloseableRegistry<V> {
            val reg = CloseableRegistry<V>(name, expose)
            reg.open()
            return register(name, reg) as CloseableRegistry<V>
        }

        private fun <V : Clone> makeImmutable(
            name: Identifier,
            expose: Boolean = true,
            delegate: DelegatedRegistry<V>.(Identifier, V) -> Unit = { _, _ -> }
        ) = register(name, ImmutableRegistry(name, delegate, expose)) as Registry<V>

        private fun <V> makeDelegated(name: Identifier, expose: Boolean = true, delegate: DelegatedRegistry<V>.(Identifier, V) -> Unit) =
            register(name, DelegatedRegistry(name, delegate, expose)) as Registry<V>

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
        val FORGE_RECIPE = makeDefaulted<ForgeRecipe>(id("forge_recipe"))
        val LOOT_POOL = makeDefaulted<LootPool>(id("loot_pool"))
        val SEA_CREATURE = makeDefaulted<SeaCreature>(id("sea_creature"))
        val TROPHY_FISH = makeDefaulted<TrophyFish>(id("trophy_fish"))
        val FISHING_TREASURE = makeDefaulted<FishingTreasure>(id("fishing_treasure"))
        val SLAYER = makeDelegated<Slayer>(id("slayer")) { _, slayer ->
            val registeredAbilities = mutableListOf<String>()
            var minisRegistered = 0
            for (tier in slayer.tiers) {
                (slayer.bossModelForTier(tier) as EntityBase).register(id("${slayer.id}_$tier"))
                for (ability in slayer.abilitiesForTier(tier)) {
                    if (registeredAbilities.contains(ability.abilityId))
                        continue
                    ability.listenerRegister(ability)
                    registeredAbilities.add(ability.abilityId)
                }
                for (mini in slayer.minisForTier(tier)) {
                    (mini as EntityBase).register(id("${slayer.id}_miniboss_$minisRegistered"))
                    minisRegistered++
                }
            }
        }
        val COSMETIC = makeDefaulted<Cosmetic>(id("cosmetic"))
        val MODEL_PREDICATES = makeDelegated<Model>(id("model"), false) { _, model ->
            CMDGenerator.enqueue(model)
            if (model.to.startsWith("macrocosm:") && model !is RawModel)
                TexturedModelGenerator.enqueue(model)
        }
        val RESOURCE_GENERATORS = makeDefaulted<ResGenerator>(id("resource_gen"), false)
        val SPELL = makeDefaulted<Spell>(id("spell"))
        val SCROLL_RECIPE = makeDefaulted<ScrollRecipe>(id("scroll_recipe"))
        val BAZAAR_ELEMENTS = makeImmutable<MacrocosmItem>(id("bazaar_elements"), false)
        val BAZAAR_ELEMENTS_REF = makeDefaulted<Identifier>(id("bazaar_elements_ref"), false)

        override fun dumpToFile(file: Path) {

        }
    }
}
