package space.maxus.macrocosm.pets

import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.LevelingTable

@Suppress("MemberVisibilityCanBePrivate")
abstract class Pet(val id: Identifier, val name: String, val headSkin: String): Listener {
    abstract val effects: PetEffects
    abstract val table: LevelingTable

    protected fun buildName(level: Int, player: MacrocosmPlayer, rarity: Rarity): Component = comp("<dark_gray>[<gray>Lvl $level<dark_gray>] <${rarity.color.asHexString()}> ${player.paper?.name}'s $name")

    fun spawn(player: MacrocosmPlayer, rarity: Rarity, level: Int): PetInstance? {
        val paper = player.paper ?: return null
        val stand = paper.world.spawnEntity(paper.location, EntityType.ARMOR_STAND) as ArmorStand
        stand.isInvulnerable = true
        stand.isVisible = false
        stand.isSmall = true
        // stand.isMarker = true
        stand.setGravity(false)
        stand.isCustomNameVisible = true
        stand.customName(buildName(level, player, rarity))
        stand.persistentDataContainer.set(NamespacedKey(Macrocosm, "ignore_damage"), PersistentDataType.BYTE, 0)
        stand.equipment.helmet = ItemValue.placeholderHead(headSkin, "PetEntity", "")

        val instance = PetInstance(stand.uniqueId, id, rarity)
        player.activePet = instance
        instance.teleport(player)
        instance.floatTick(player, stand.location)
        return instance
    }
}
