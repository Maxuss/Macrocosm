package space.maxus.macrocosm.loot

import net.minecraft.util.Mth
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.collections.CollectionSection
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.item.ItemPet
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.slayer.ui.slayerLevelBuff
import space.maxus.macrocosm.util.general.id
import kotlin.math.roundToInt
import kotlin.random.Random

class LootPool private constructor(val drops: List<Drop>) {
    companion object {
        fun of(vararg drops: Drop) = LootPool(drops.toList().filter { it.amount.last > 0 })
    }

    fun roll(mf: Float = 0f, player: MacrocosmPlayer? = null) = drops.filter { drop ->
        val mfMod = mf / 100.0
        val chance = if (player == null) drop.chance * (1 + mfMod) else {
            val base = drop.chance * (1 + mfMod)
            val slayer = player.slayerQuest
            if (slayer == null)
                base
            else {
                val slayerLevel = player.slayers[slayer.type]!!
                val rngSelected = slayerLevel.rng[slayer.type]!!
                val dropSelected = rngSelected.selectedRngDrop
                val actualDrop = slayer.type.slayer.drops[dropSelected].drop
                if (actualDrop == drop) {
                    // applying calculations
                    val expToDrop = (((1 / base) * 900) * slayerLevelBuff[slayerLevel.level]).roundToInt()
                    val newChance = base + (rngSelected.expAccumulated / expToDrop) * .03
                    newChance
                } else {
                    base
                }
            }
        }
        Random.nextDouble() <= chance
    }

    fun roll(player: MacrocosmPlayer?, applyFortune: Boolean = true): List<ItemStack?> {
        val stats = player?.stats()
        val roll = roll(mf = stats?.magicFind ?: 0f)
        return roll.map {
            if (it.item.namespace == "minecraft") {
                val mat = Material.valueOf(it.item.path.uppercase())
                var amount = it.amount.random()
                if (applyFortune) {
                    val collType = CollectionType.from(mat)
                    if (collType != null) {
                        player?.addCollectionAmount(collType, amount)
                        var boost = 1
                        val fortune = when (collType.inst.section) {
                            CollectionSection.FARMING -> stats?.farmingFortune
                            CollectionSection.MINING -> stats?.miningFortune
                            CollectionSection.FORAGING -> stats?.foragingFortune
                            CollectionSection.EXCAVATING -> stats?.excavatingFortune
                            else -> null
                        } ?: 0f
                        boost += (fortune / 100).roundToInt()

                        amount = Mth.floor((amount * (boost + fortune / 100f)))
                        amount += if (Random.nextFloat() < ((fortune % 100) / 100f)) 1 else 0
                    }
                }
                val item = ItemStack(mat, amount)
                if (player?.paper != null) {
                    it.rarity.announceEntityDrop(player.paper!!, item.macrocosm ?: return@map null, it)
                }
                item.macrocosm?.build(player) ?: return@map null
            } else if (it.item.path.contains("pet")) {
                val (id, rarity) = it.item.path.split("@")
                val newId = id(id)
                val basePet = Registry.ITEM.find(newId) as ItemPet
                val rar = Rarity.valueOf(rarity.uppercase())
                basePet.stored = StoredPet(newId, rar, 1, .0)
                basePet.rarity = rar
                if (player?.paper != null) {
                    it.rarity.announceEntityDrop(player.paper!!, basePet, it)
                }
                basePet.build(player)
            } else {
                val item = Registry.ITEM.find(it.item)
                var amount = it.amount.random()
                val collType = CollectionType.fromIdentifier(it.item)
                if (collType != null) {
                    player?.addCollectionAmount(collType, amount)
                    var boost = 1
                    var fortune = when (collType.inst.section) {
                        CollectionSection.FARMING -> stats?.farmingFortune
                        CollectionSection.MINING -> stats?.miningFortune
                        CollectionSection.FORAGING -> stats?.foragingFortune
                        CollectionSection.EXCAVATING -> stats?.excavatingFortune
                        else -> null
                    } ?: 0f

                    while (fortune >= 100f) {
                        boost += 1
                        fortune -= 100f
                    }
                    amount = Mth.floor((amount * (boost + fortune / 100f)))
                }
                if (player?.paper != null) {
                    it.rarity.announceEntityDrop(player.paper!!, item, it)
                }
                (item.build(player) ?: return@map null).apply { this.amount = amount }
            }
        }
    }

    fun rollRaw(mf: Float = 0f) = roll(mf).map {
        if (it.item.path == "minecraft")
            ItemStack(Material.valueOf(it.item.path.uppercase()), it.amount.random())
        else
            Registry.ITEM.find(it.item).build()!!.apply { amount = it.amount.random() }
    }
}
