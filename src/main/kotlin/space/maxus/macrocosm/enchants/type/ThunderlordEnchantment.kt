package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

object ThunderlordEnchantment :
    EnchantmentBase("Thunderlord", "", 1..8, ItemType.melee(), conflicts = listOf("THUNDERBOLT")) {
    override fun description(level: Int): List<Component> {
        val str =
            "<gray>Strikes a monster with lightning every <green>3<gray> consecutive hits. Lightning deals <green>${100 + ((level - 1) * 25)}%<gray> of your ${Statistic.STRENGTH.display}<gray>."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        val tlHits = pdc[NamespacedKey(Macrocosm, "tl_hits"), PersistentDataType.BYTE]
        if (tlHits == null) {
            pdc[NamespacedKey(Macrocosm, "tl_hits"), PersistentDataType.BYTE] = 1
            return
        }
        if (tlHits >= 3) {
            pdc.remove(NamespacedKey(Macrocosm, "tl_hits"))
            e.damaged.location.world.strikeLightningEffect(e.damaged.location)
            val stats = e.player.stats() ?: return
            val damage = stats.strength * (1 + ((lvl - 1) * 0.25f))
            e.damaged.macrocosm!!.damage(damage, e.player.paper)
            DamageHandlers.summonDamageIndicator(e.damaged.location, damage, DamageType.ELECTRIC)
        } else {
            pdc[NamespacedKey(Macrocosm, "tl_hits"), PersistentDataType.BYTE] = (tlHits + 1).toByte()
        }
    }
}

object ThunderboltEnchantment :
    EnchantmentBase("Thunderbolt", "", 1..8, ItemType.melee(), conflicts = listOf("THUNDERLORD")) {
    override fun description(level: Int): List<Component> {
        val str =
            "<gray>Strikes nearby monsters with lightning every <green>3<gray> consecutive hits on the <blue>same<gray> enemy. Lightning deals <green>${100 + ((level - 1) * 10)}%<gray> of your ${Statistic.CRIT_DAMAGE.display}<gray>."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        val tlHits = pdc[NamespacedKey(Macrocosm, "tb_hits"), PersistentDataType.BYTE]
        if (tlHits == null) {
            pdc[NamespacedKey(Macrocosm, "tb_hits"), PersistentDataType.BYTE] = 1
            return
        }
        if (tlHits >= 3) {
            pdc.remove(NamespacedKey(Macrocosm, "tb_hits"))
            var counter = 0
            val stats = e.player.stats() ?: return
            for (entity in e.damaged.world.getNearbyEntities(e.damaged.location, 10.0, 4.0, 10.0) {
                it is LivingEntity && it !is Player && it !is ArmorStand && ++counter < 5
            }) {
                val living = entity as LivingEntity
                living.world.strikeLightningEffect(living.location)
                val damage = stats.critDamage * (1 + ((lvl - 1) * 0.1f))
                living.macrocosm!!.damage(damage, e.player.paper)
                DamageHandlers.summonDamageIndicator(living.location, damage, DamageType.ELECTRIC)
            }
        } else {
            pdc[NamespacedKey(Macrocosm, "tb_hits"), PersistentDataType.BYTE] = (tlHits + 1).toByte()
        }
    }

}
