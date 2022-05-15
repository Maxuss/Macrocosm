package space.maxus.macrocosm.ability.types

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.loot.Drop
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.MacrocosmDrop
import space.maxus.macrocosm.events.BlockDropItemsEvent
import space.maxus.macrocosm.stats.Statistic

object EmeraldArmorBonus : FullSetBonus(
    "Emerald Affection",
    "Grants <gold>+200 ${Statistic.MINING_FORTUNE.display}<gray> when mining emerald ore."
) {
    override fun registerListeners() {
        listen<BlockDropItemsEvent> { e ->
            if (e.block.type != Material.EMERALD_ORE)
                return@listen
            if (!ensureSetRequirement(e.player))
                return@listen
            val pool = mutableListOf<Drop>()
            for (drop in e.pool.drops) {
                val first = drop.amount.first + 2
                val last = drop.amount.last + 2
                pool.add(MacrocosmDrop(drop.item, drop.rarity, drop.chance, first..last))
            }
            e.pool = LootPool.of(*pool.toTypedArray())
            particle(Particle.VILLAGER_HAPPY) {
                amount = 5
                offset = Vector.getRandom()
                spawnAt(e.block.location)
            }
        }
    }
}
