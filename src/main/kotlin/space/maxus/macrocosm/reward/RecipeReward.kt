package space.maxus.macrocosm.reward

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.recipes.RecipeRegistry
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.Identifier

class RecipeReward(val recipe: Identifier, override val isHidden: Boolean = false): Reward {
    override fun reward(player: MacrocosmPlayer, lvl: Int) {
        player.unlockedRecipes.add(recipe)
    }

    override fun display(lvl: Int): Component {
        val rec = RecipeRegistry.find(recipe)!!
        return comp("${rec.resultItem().displayName().str()}<gray> Recipe").noitalic()
    }
}
