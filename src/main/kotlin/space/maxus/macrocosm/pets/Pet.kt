package space.maxus.macrocosm.pets

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.item.*
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.LevelingTable
import space.maxus.macrocosm.util.id

@Suppress("MemberVisibilityCanBePrivate")
abstract class Pet(
    val id: Identifier,
    val name: String,
    val headSkin: String,
    val preferredSkill: SkillType,
    val abilities: List<PetAbility> = listOf(),
    private val baseStats: Statistics = Statistics.zero(),
    private val baseSpecials: SpecialStatistics = SpecialStatistics()
): Listener {
    abstract val effects: PetEffects
    abstract val table: LevelingTable

    fun abilitiesForRarity(rarity: Rarity): List<PetAbility> {
        return if(rarity <= Rarity.UNCOMMON)
            listOf(abilities[0])
        else if(rarity <= Rarity.EPIC)
            listOf(abilities[0], abilities[1])
        else if(rarity == Rarity.LEGENDARY)
            listOf(abilities[0], abilities[1], abilities[2])
        else abilities
    }

    fun registerItem() {
        ItemRegistry.register(id, PetItem(id, name, headSkin))
    }

    fun buildItem(player: MacrocosmPlayer, value: StoredPet): ItemStack {
        val found = ItemRegistry.findOrNull(this.id) as? PetItem
        if(found == null) {
            val item = PetItem(
                id(value.id.path),
                name,
                headSkin,
                value
            )
            ItemRegistry.register(id, item)
            return item.build(player)!!
        }
        found.stored = value
        found.rarity = value.rarity
        return found.build(player)!!
    }

    fun ensureRequirement(player: MacrocosmPlayer, ability: String): Pair<Boolean, StoredPet?> {
        player.paper ?: return Pair(false, null)
        val active = player.activePet?.base
        val ref = player.activePet?.referring(player)
        if(active == this.id && ref != null && abilitiesForRarity(ref.rarity).map { it.name }.contains(ability))
            return Pair(true, player.ownedPets[player.activePet!!.hashKey])
        return Pair(false, null)
    }

    protected fun buildName(level: Int, player: MacrocosmPlayer, rarity: Rarity): Component = comp("<dark_gray>[<gray>Lvl $level<dark_gray>] <${rarity.color.asHexString()}> ${player.paper?.name}'s $name")

    fun stats(level: Int, rarity: Rarity): Statistics {
        val clone = baseStats.clone()
        clone.multiply((level / 100f) * (rarity.ordinal + 1))
        return clone
    }

    fun specialStats(level: Int, rarity: Rarity): SpecialStatistics {
        val clone = baseSpecials.clone()
        clone.multiply((level / 100f) * (rarity.ordinal + 1))
        return clone
    }

    fun spawn(player: MacrocosmPlayer, key: String): PetInstance? {
        if(player.activePet != null) {
            if(player.activePet!!.hashKey == key) {
                player.activePet!!.despawn(player)
            } else
                player.activePet?.despawn(player)
        }
        val paper = player.paper ?: return null
        val stored = player.ownedPets[key]!!
        val stand = paper.world.spawnEntity(paper.location, EntityType.ARMOR_STAND) as ArmorStand
        stand.isInvulnerable = true
        stand.isVisible = false
        stand.isSmall = true
        // stand.isMarker = true
        stand.setGravity(false)
        stand.isCustomNameVisible = true
        stand.customName(buildName(stored.level, player, stored.rarity))
        stand.persistentDataContainer.set(NamespacedKey(Macrocosm, "ignore_damage"), PersistentDataType.BYTE, 0)
        stand.equipment.helmet = ItemValue.placeholderHead(headSkin, "PetEntity", "")

        player.sendMessage("<green>You spawned your <${stored.rarity.color.asHexString()}>$name<green>.")
        val instance = PetInstance(stand.uniqueId, id, key)
        player.activePet = instance
        instance.teleport(player)
        instance.floatTick(player, stand.location)
        return instance
    }
}
