package space.maxus.macrocosm.enchants.type

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.macrocosm
import kotlin.random.Random

object ExperienceEnchantment : EnchantmentBase(
    "Experience",
    "Grants a <green><multiplier_whole>%<gray> chance for mobs and ores to drop <blue>double<gray> experience orbs.",
    1..4,
    listOf(
        ItemType.PICKAXE,
        ItemType.HOE,
        ItemType.AXE,
        ItemType.SHOVEL,
        ItemType.GAUNTLET,
        ItemType.SWORD,
    ),
    multiplier = 0.125f
) {
    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        if (e.player.macrocosm == null)
            return
        val player = e.player.macrocosm!!
        val (ok, lvl) = ensureRequirements(player, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        if (!ok)
            return
        val chance = .125f * lvl
        e.expToDrop *= if (Random.nextFloat() < chance) 2 else 1
    }

    @EventHandler
    fun onKill(e: EntityDeathEvent) {
        val cause = e.entity.lastDamageCause
        if (cause !is EntityDamageByEntityEvent || cause.damager !is Player)
            return
        val player = cause.damager as Player
        val mc = player.macrocosm ?: return
        val (ok, lvl) = ensureRequirements(mc, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        if (!ok)
            return
        val chance = .125f * lvl
        e.droppedExp *= if (Random.nextFloat() < chance) 2 else 1
    }
}
