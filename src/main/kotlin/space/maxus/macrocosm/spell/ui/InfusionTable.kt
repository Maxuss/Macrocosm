package space.maxus.macrocosm.spell.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.SpellScroll
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.slayer.ui.LinearInventorySlots
import space.maxus.macrocosm.spell.essence.EssenceType
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.metrics.report
import java.util.concurrent.atomic.AtomicReference

fun displaySelectEssence(
    p: MacrocosmPlayer,
    scroll: AtomicReference<Boolean>,
    selectedEssence: HashMap<InventorySlot, EssenceType>,
    currentlySelectedSlot: SingleInventorySlot<out ForInventory>
): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    // essence selection
    title = text("Select Essence")
    defaultPage = 0

    page(0) {
        val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
        placeholder(Slots.All, glass)

        val cmp = createCompound<String>(iconGenerator = {
            if (it == "NULL")
                glass
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
        }, onClick = { e, it ->
            e.bukkitEvent.isCancelled = true
            if (it == "NULL")
                return@createCompound
            val ty = EssenceType.valueOf(it)
            val availableAmount = p.availableEssence[ty]!!
            if (availableAmount <= 0)
                return@createCompound
            if (availableAmount < 5) {
                p.sendMessage("<red>Not enough essence! Requires at least 5 essence.")
                return@createCompound
            }
            selectedEssence[currentlySelectedSlot.inventorySlot] = ty
            e.player.openGUI(displayInfusionTable(p, scroll, selectedEssence))
        })
        compoundSpace(
            LinearInventorySlots(
                listOf(
                    InventorySlot(5, 3),
                    InventorySlot(5, 5),
                    InventorySlot(5, 7),
                    InventorySlot(3, 3),
                    InventorySlot(3, 5),
                    InventorySlot(3, 7),
                    InventorySlot(4, 5)
                )
            ),
            cmp
        )

        cmp.addContent(
            listOf(
                EssenceType.FIRE.name, EssenceType.WATER.name, EssenceType.FROST.name,
                EssenceType.LIFE.name, EssenceType.SHADE.name, EssenceType.DEATH.name,
                EssenceType.CONNECTION.name
            )
        )
    }
}

fun displayInfusionTable(
    p: MacrocosmPlayer,
    scroll: AtomicReference<Boolean> = AtomicReference(false),
    selectedEssence: HashMap<InventorySlot, EssenceType> = hashMapOf()
): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Infusion Table")

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
    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
        slots.forEach { slot ->
            val ty = selectedEssence[slot.inventorySlot]
            val item = if (ty == null) essenceEmpty else ItemValue.placeholder(ty.displayItem, "${ty.display} Essence")
            button(slot, item) { e ->
                e.bukkitEvent.isCancelled = true
                if (selectedEssence[slot.inventorySlot] != null) {
                    e.bukkitEvent.inventory.setItem(
                        slot.inventorySlot.realSlotIn(InventoryDimensions(9, 6))!!,
                        essenceEmpty
                    )
                    sound(Sound.ITEM_BOTTLE_EMPTY) {
                        volume = 2f
                        playFor(e.player)
                    }
                }
                e.player.openGUI(displaySelectEssence(p, scroll, selectedEssence, slot))
            }
        }
        freeSlot(Slots.RowThreeSlotFive)
        button(Slots.RowTwoSlotFive, infuse) { e ->
            e.bukkitEvent.isCancelled = true
            val realSlot = Slots.RowThreeSlotFive.inventorySlot.realSlotIn(InventoryDimensions(9, 6))!!
            val curs = e.bukkitEvent.inventory.getItem(realSlot)
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
                e.bukkitEvent.inventory.setItem(
                    realSlot,
                    SpellScroll().apply { spell = resultSpell }.build(p)
                        ?: report("Got null as build result for spell scroll!") { return@button })
                selectedEssence.forEach { (eSlot, eTy) ->
                    p.availableEssence[eTy] = p.availableEssence[eTy]!! - 1
                    e.bukkitEvent.inventory.setItem(eSlot.realSlotIn(InventoryDimensions(9, 6))!!, essenceEmpty)
                }
                selectedEssence.clear()
                sound(Sound.BLOCK_END_PORTAL_FRAME_FILL) {
                    pitch = 0f
                    volume = 5f

                    playFor(e.player)
                }
            }
        }
    }
}

private val slots = listOf(
    Slots.RowTwoSlotThree,
    Slots.RowTwoSlotSeven,
    Slots.RowFourSlotThree,
    Slots.RowFourSlotSeven,
)
