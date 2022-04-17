package space.maxus.macrocosm.entity.loot

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import kotlin.random.Random

class LootPool private constructor(private val drops: List<EntityDrop>) {
    companion object {
        fun of(vararg drops: EntityDrop) = LootPool(drops.toList())
    }

    fun roll(mf: Float = 0f) = drops.filter { drop ->
        val mfMod = mf / 100.0
        val chance = drop.chance * (1 + mfMod)
        Random.nextDouble() <= chance
    }

    fun roll(player: MacrocosmPlayer?): List<ItemStack?> {
        val roll = roll(player?.calculateStats()?.magicFind ?: 0f)
            return roll.map {
                if (it.item.startsWith('@')) {
                    val item = ItemStack(Material.valueOf(it.item.replace("@", "")), it.amount.random())
                    if (player?.paper != null) {
                        it.rarity.announceEntityDrop(player.paper!!, item.macrocosm ?: return@map null)
                    }
                    item.macrocosm?.build() ?: return@map null
                } else {
                    val item = ItemRegistry.find(it.item)
                    if (player?.paper != null) {
                        it.rarity.announceEntityDrop(player.paper!!, item)
                    }
                    (item.build() ?: return@map null).apply { amount = it.amount.random() }
                }
            }
    }

    fun rollRaw(mf: Float = 0f) = roll(mf).map {
        if(it.item.startsWith('@'))
            ItemStack(Material.valueOf(it.item.replace("@", "")), it.amount.random())
        else
            ItemRegistry.find(it.item).build()!!.apply { amount = it.amount.random() }
    }
}
