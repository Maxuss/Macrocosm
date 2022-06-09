package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.comp

class RecipeReward(val recipe: Identifier, override val isHidden: Boolean = false) : Reward {
    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        player.unlockedRecipes.add(recipe)
    }

    override fun display(lvl: Int): Component {
        val it = Registry.RECIPE.find(recipe).resultMacrocosm()
        return it.name.color(it.rarity.color).append(comp("<gray> Recipe")).noitalic()
    }
}
