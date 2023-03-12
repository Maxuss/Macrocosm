package space.maxus.macrocosm.spell.ui

import net.axay.kspigot.gui.InventoryDimensions
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.SpellScroll
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.animation.UIRenderHelper
import space.maxus.macrocosm.ui.components.LinearComponentSpace
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.metrics.report
import java.util.concurrent.atomic.AtomicReference

fun displaySelectEssence(
    p: MacrocosmPlayer,
    scroll: AtomicReference<Boolean>,
    selectedEssence: HashMap<Slot, EssenceType>,
    currentlySelectedSlot: Slot
): MacrocosmUI = macrocosmUi("display_essence_select", UIDimensions.FIVE_X_NINE) {
    // essence selection
    title = "Select Essence"

    page {
        background()

        compound(LinearComponentSpace(
            listOf(
                Slot(1, 2),
                Slot(1, 4),
                Slot(1, 6),
                Slot(3, 2),
                Slot(3, 4),
                Slot(3, 6),
                Slot(2, 4)
            ).map { it.value }
        ), listOf(
            EssenceType.FIRE.name, EssenceType.WATER.name, EssenceType.FROST.name,
            EssenceType.LIFE.name, EssenceType.SHADE.name, EssenceType.DEATH.name,
            EssenceType.CONNECTION.name
        ), {
            if (it == "NULL")
                UIRenderHelper.dummy(Material.GRAY_STAINED_GLASS_PANE)
            else {
                val ty = EssenceType.valueOf(it)
                val amount = p.availableEssence[ty]!!
                if (amount > 0) {
                    val item = ItemValue.placeholder(ty.displayItem, "${ty.display}<white>: $amount")
                    item.meta {
                        val newLore = mutableListOf<Component>()
                        ty.descript(newLore)
                        newLore.addAll(
                            listOf(
                                "",
                                "<gray>You have: <green>$amount<gray> Essence"
                            ).map { ele -> text(ele).noitalic() }
                        )
                        lore(newLore)
                    }
                    item
                } else
                    ItemValue.placeholderDescripted(
                        Material.GRAY_TERRACOTTA,
                        "<gray>Unknown Essence<white>: 0",
                        "This is an unknown essence type!",
                        "Find it somewhere in the world",
                        "first to reveal it."
                    )
            }
        }, { data, it ->
            if (it == "NULL")
                return@compound
            val ty = EssenceType.valueOf(it)
            val availableAmount = p.availableEssence[ty]!!
            if (availableAmount <= 0)
                return@compound
            if (availableAmount < 5) {
                p.sendMessage("<red>Not enough essence! Requires at least 5 essence.")
                return@compound
            }
            selectedEssence[currentlySelectedSlot] = ty
            data.instance.switch(displayInfusionTable(p, scroll, selectedEssence))
        })
    }
}

fun displayInfusionTable(
    p: MacrocosmPlayer,
    scroll: AtomicReference<Boolean> = AtomicReference(false),
    selectedEssence: HashMap<Slot, EssenceType> = hashMapOf()
): MacrocosmUI = macrocosmUi("infusion_table", UIDimensions.FIVE_X_NINE) {
    title = "Infusion Table"

    val essenceEmpty = ItemValue.placeholderDescripted(
        Material.LIGHT_GRAY_STAINED_GLASS_PANE,
        "<gray>Essence Slot",
        "<dark_gray> > <gray>No Essence",
        " ",
        "<yellow>Click to add essence!"
    )
    val infuse = ItemValue.placeholderDescripted(
        Material.GREEN_TERRACOTTA,
        "<green>Infuse",
        "Place essence in the essence",
        "slots and an empty spell",
        "scroll in the slot above to",
        "infuse it!"
    )

    onClose = {
        val item = it.inventory.getItem(Slot.RowFourSlotFive.value)
        it.paper.giveOrDrop(item ?: ItemStack(Material.AIR))
    }

    page {
        background()

        slots.forEach { slot ->
            val ty = selectedEssence[slot]
            val item = if (ty == null) essenceEmpty else ItemValue.placeholder(ty.displayItem, "${ty.display} Essence")
            button(slot, item) { data ->
                if (selectedEssence[slot] != null) {
                    data.bukkit.inventory.setItem(
                        slot.value,
                        essenceEmpty
                    )
                    sound(Sound.ITEM_BOTTLE_EMPTY) {
                        volume = 2f
                        playFor(data.paper)
                    }
                }
                data.instance.switch(displaySelectEssence(p, scroll, selectedEssence, slot))
            }
        }
        val slot = storageSlot(Slot.RowFourSlotFive, fits = { it.macrocosm.let { s -> s is SpellScroll && s.spell == null } }, onPut = { data, _ ->
            scroll.set(true)
            data.instance.reload()
        }, onTake = { data, _ ->
            scroll.set(false)
            data.instance.reload()
        })

        button(Slot.RowFiveSlotFive, infuse) { data ->
            val realSlot = Slots.RowThreeSlotFive.inventorySlot.realSlotIn(InventoryDimensions(9, 6))!!
            val curs = data.bukkit.inventory.getItem(realSlot)
            scroll.set(false)
            if (curs.isAirOrNull() || curs?.macrocosm !is SpellScroll) {
                return@button
            } else {
                val mc = curs.macrocosm!! as SpellScroll
                if (mc.spell != null)
                    return@button
                val playerLvl = p.skills.level(SkillType.MYSTICISM)
                val providedEssence = selectedEssence.values
                val available = Registry.SCROLL_RECIPE.iter().filter { (_, rec) -> playerLvl >= rec.level }.entries
                val applicable = available.filter { (_, rec) ->
                    rec.requirements.all { (essence, requiredAmount) ->
                        providedEssence.count { it == essence } >= requiredAmount
                    }
                }
                val recipe = applicable.maxByOrNull { (_, rec) -> rec.requirements.size }?.value ?: run {
                    p.sendMessage("<red>Unknown recipe")
                    return@button
                }
                val resultSpell = Registry.SPELL.findOrNull(recipe.result)
                    ?: report("Invalid spell identifier in scroll recipe: ${recipe.result}!") { return@button }
                slot.stored = SpellScroll().apply { spell = resultSpell }.build(p)
                    ?: report("Got null as build result for spell scroll!") { return@button }
                selectedEssence.forEach { (_, eTy) ->
                    p.availableEssence[eTy] = p.availableEssence[eTy]!! - 1
                }
                selectedEssence.clear()
                sound(Sound.BLOCK_END_PORTAL_FRAME_FILL) {
                    pitch = 0f
                    volume = 5f

                    playFor(data.paper)
                }
                data.instance.reload()
            }
        }
    }
}

private val slots = listOf(
    Slot.RowFourSlotThree,
    Slot.RowFourSlotSeven,
    Slot.RowTwoSlotThree,
    Slot.RowTwoSlotSeven,
)
