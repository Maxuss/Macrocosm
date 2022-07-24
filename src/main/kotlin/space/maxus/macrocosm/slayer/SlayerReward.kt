package space.maxus.macrocosm.slayer

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.reward.Reward

data class SlayerReward(val display: RewardDisplay, val rewards: List<Reward>)

interface RewardDisplay {
    fun display(name: String, vararg lore: String): ItemStack
}

data class MaterialDisplay(val material: Material) : RewardDisplay {
    override fun display(name: String, vararg lore: String): ItemStack {
        return ItemValue.placeholderDescripted(material, name, *lore)
    }
}

data class SkullDisplay(val skin: String) : RewardDisplay {
    override fun display(name: String, vararg lore: String): ItemStack {
        return ItemValue.placeholderHeadDesc(skin, name, *lore)
    }
}
