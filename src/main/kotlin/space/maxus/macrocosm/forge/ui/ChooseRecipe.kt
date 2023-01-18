package space.maxus.macrocosm.forge.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.ability.types.accessory.OldBlueprints
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.forge.ActiveForgeRecipe
import space.maxus.macrocosm.forge.ForgeRecipe
import space.maxus.macrocosm.forge.ForgeType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.mapPaired
import space.maxus.macrocosm.util.toFancyString
import java.time.Duration
import java.time.Instant

fun recipeChoose(player: MacrocosmPlayer, forge: ForgeType): GUI<*> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("${forge.displayName} Recipes")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val recipeCompound = createRectCompound<ForgeRecipe>(Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
            iconGenerator = { recipe ->
                if (player.skills.level(forge.skill) < recipe.requiredLvl) {
                    ItemValue.placeholderDescripted(
                        Material.WHITE_STAINED_GLASS_PANE,
                        "<red>???",
                        "<red>Requires ${forge.skill.inst.name} LVL ${recipe.requiredLvl}"
                    )
                } else {
                    val (resId, resAmount) = recipe.result
                    val resItem = Registry.ITEM.find(resId)
                    val built = resItem.build(player)!!
                    built.meta {
                        if (resAmount > 1) {
                            displayName(displayName()!!.append(text(" <gray>${resAmount}x").noitalic()))
                        }
                        val loreClone = lore()!!
                        loreClone.addAll(
                            listOf(
                                "",
                                "<yellow>Items Required:",
                                *recipe.input.mapPaired(::unwrapIngredient).toTypedArray(),
                                "",
                                "<gray>Duration: <aqua>${Duration.ofSeconds(recipe.length).toFancyString()}",
                                "",
                                "<yellow>Click to start!"
                            ).map { text -> text(text).noitalic() }
                        )
                        lore(loreClone)
                    }
                    built
                }
            },
            onClick = { e, recipe ->
                e.bukkitEvent.isCancelled = true
                if (player.activeForgeRecipes.size >= 5)
                    player.sendMessage("<red>You have reached forge recipe limit!")
                else {
                    if (recipe.input.all { (id, amount) ->
                            val it = Registry.ITEM.find(id).build(player)!!
                            e.player.inventory.containsAtLeast(it, amount)
                        }) {
                        recipe.input.forEach { (itemId, itemAmount) ->
                            val it = Registry.ITEM.find(itemId).build(player)!!
                            it.amount = itemAmount
                            e.player.inventory.removeItemAnySlot(it)
                        }
                        sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                            pitch = 2f
                            volume = 2f

                            playFor(e.player)
                        }
                        val addMillis = if (OldBlueprints.hasAccs(player)) recipe.length * 250 else 0
                        player.activeForgeRecipes.add(
                            ActiveForgeRecipe(
                                Registry.FORGE_RECIPE.byValue(recipe)!!,
                                Instant.now().toEpochMilli() + addMillis
                            )
                        )
                        e.player.openGUI(displayForge(player, forge))
                    } else {
                        player.sendMessage("<red>Not enough ingredients!")
                    }
                }
            }
        )
        recipeCompound.addContent(Registry.FORGE_RECIPE.iter().filter { it.value.type == forge }.values)
        compoundScroll(
            Slots.RowOneSlotEight,
            ItemValue.placeholder(Material.ARROW, "<red>Previous Page"),
            recipeCompound,
            reverse = true
        )
        compoundScroll(Slots.RowOneSlotNine, ItemValue.placeholder(Material.ARROW, "<green>Next Page"), recipeCompound)
        button(
            Slots.RowOneSlotOne,
            ItemValue.placeholderDescripted(Material.ARROW, "<red>Back", "Back to ${forge.displayName}")
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(displayForge(player, forge))
        }
    }
}

private fun unwrapIngredient(ingredient: Pair<Identifier, Int>): String {
    val item = Registry.ITEM.find(ingredient.first)
    val name = if (ingredient.second > 1) item.buildName()
        .append(text(" <gray>${ingredient.second}x").noitalic()) else item.buildName()
    return name.str()
}
