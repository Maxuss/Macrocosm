package space.maxus.macrocosm.reward

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.text

class ItemReward(val item: Identifier, val amount: Int = 1, override val isHidden: Boolean = false) : Reward {
    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        player.paper!!.inventory.addItem(
            Registry.ITEM.find(item).build(player)!!.apply { amount = this@ItemReward.amount })
    }

    override fun display(lvl: Int): Component {
        val item = Registry.ITEM.find(item)
        return item.name.color(item.rarity.color).append(if (amount > 1) text("<gray>${amount}x") else "".toComponent())
    }
}
