package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.pluginManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.getId
import java.util.concurrent.ConcurrentHashMap

val LivingEntity.macrocosm: MacrocosmEntity? get() = EntityRegistry.toMacrocosm(this)

@Suppress("unused")
object EntityRegistry {
    var DO_MOJANG_API_REQUESTS = true

    private val entities: ConcurrentHashMap<Identifier, MacrocosmEntity> = ConcurrentHashMap(hashMapOf())
    private val disguises: ConcurrentHashMap<Identifier, String> = ConcurrentHashMap(hashMapOf())
    private val sounds: ConcurrentHashMap<Identifier, EntitySoundBank> = ConcurrentHashMap(hashMapOf())

    fun registerDisguise(name: Identifier, skin: String, raw: Boolean = false) {
        if (raw) {
            disguises[name] = skin
            return
        }
        // probably not that efficient, however it should only be done at startup,
        // and concurrently, hopefully not that much of a logic speed impact
        disguises[name] = skin.replace("\n", "").replace("\\s+".toRegex(), "")
    }

    fun register(name: Identifier, entity: MacrocosmEntity): MacrocosmEntity {
        if (entities.containsKey(name))
            return entity
        entities[name] = entity
        pluginManager.registerEvents(entity, Macrocosm)
        return entity
    }

    fun registerSounds(name: Identifier, bank: EntitySoundBank): EntitySoundBank {
        sounds[name] = bank
        return bank
    }

    fun hasSounds(id: Identifier) = sounds.containsKey(id)

    fun findSounds(id: Identifier) = sounds[id]!!

    fun shouldDisguise(id: Identifier) = disguises.containsKey(id)

    fun findDisguise(id: Identifier) = disguises[id]!!

    fun nameOf(ref: MacrocosmEntity) = entities.filter { (_, v) -> v == ref }.map { (k, _) -> k }.firstOrNull()

    fun find(name: Identifier) = entities[name]

    fun toMacrocosm(entity: LivingEntity): MacrocosmEntity? {
        if (entity is Player)
            return null
        val tag = entity.readNbt()
        if (tag.contains(MACROCOSM_TAG)) {
            val id = tag.getCompound(MACROCOSM_TAG).getId("ID")
            if (id.namespace != "minecraft") {
                val custom = CustomEntity(entity.uniqueId)
                custom.loadChanges(entity)
                return custom
            }
        }
        return VanillaEntity.from(entity)
    }
}
