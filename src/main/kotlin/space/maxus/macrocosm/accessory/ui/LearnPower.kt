package space.maxus.macrocosm.accessory.ui

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.accessory.power.PowerStone
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.giveOrDrop

private val slots = listOf(13, 14, 15, 22, 23, 24, 31, 32, 33)

fun learnPowerUi(
    player: MacrocosmPlayer,
    stones: MutableList<ItemStack> = mutableListOf()
): MacrocosmUI = macrocosmUi("learn_power", UIDimensions.SIX_X_NINE) {
    title = "Learn Power From Stones"

    onClose = {
        for (stone in stones) {
            player.paper?.giveOrDrop(stone)
        }
    }

    page {
        background()

        goBack(Slot.RowSixSlotFive, { thaumaturgyUi(player) })

        switchUi(
            Slot.RowFiveSlotSix,
            {
                powerStonesGuide(player)
            },
            ItemValue.placeholderDescripted(
                Material.KNOWLEDGE_BOOK,
                "<green>Power Stones Guide",
                "View all power stones and how to",
                "acquire them!",
                "",
                "<yellow>Click to browse!"
            )
        )

        button(
            Slot.RowThreeSlotThree, {
                val distinct = stones.mapNotNull { it.macrocosm?.id }.distinct()
                val powerId = (stones.firstOrNull()?.macrocosm as? PowerStone)?.powerId
                val power = Registry.ACCESSORY_POWER.findOrNull(powerId)
                val combatReq = if (power is StoneAccessoryPower) power.combatLevel else 0

                if (distinct.size == 1 && stones.size == 9 && !player.memory.knownPowers.contains(powerId) && player.skills.level(
                        SkillType.COMBAT
                    ) >= combatReq
                ) {
                    ItemValue.placeholderHeadDesc(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNjMjQ3YjBkODJkZDg3MTQ4Yzk2NTZhNDJlMDI0MDcwYzQ1OTcwZTExNDlmZGM1NTNlZjYzNjBmMjc5OWM2YyJ9fX0=",
                        "<green>Learn New Power",
                        "Fill all the slots on the right",
                        "with identical <blue>Power Stones",
                        "to permanently unlock their",
                        "<green>power<gray>.",
                        "",
                        "<yellow>Click to learn power!"
                    )
                } else {
                    ItemValue.placeholderHeadDesc(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBjNDUyOGU2MjJiZDMxODcyMGQzOGUwZTQ1OTllNjliZjIzMzA4Zjg5NjkzOTIwZTBlNGVjYjU1ZDFjMGJhYyJ9fX0=",
                        "<red>Learn New Power",
                        "Fill all the slots on the right",
                        "with identical <blue>Power Stones",
                        "to permanently unlock their",
                        "<green>power<gray>.",
                        "",
                        if (stones.size != 9) "<red>Missing some stones!" else if (distinct.size != 1) "<red>Yikes! Random item detected!" else if (player.memory.knownPowers.contains(
                                powerId
                            )
                        ) "<red>Already learned!" else "<red>Higher combat level required!"
                    )
                }
            }
        ) { e ->
            val distinct = stones.mapNotNull { it.macrocosm?.id }.distinct()
            val powerId = (stones.firstOrNull()?.macrocosm as? PowerStone)?.powerId
            val power = Registry.ACCESSORY_POWER.findOrNull(powerId)
            val combatReq = if (power is StoneAccessoryPower) power.combatLevel else 0

            if (distinct.size == 1 && stones.size == 9 && !player.memory.knownPowers.contains(powerId) && player.skills.level(
                    SkillType.COMBAT
                ) >= combatReq
            ) {
                sound(Sound.ENTITY_PLAYER_LEVELUP) {
                    volume = 2f
                    playAt(e.paper.location)
                }
                player.memory.knownPowers.add(powerId!!)
                e.player.sendMessage("<yellow>You permanently unlocked the <green>${power?.name}<yellow> power!")
                stones.clear()
                e.instance.reload()
            } else {
                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                    pitch = 0f
                    playFor(e.paper)
                }
                e.player.sendMessage("<red>Fill all slots in the grid with power stones to combine them!")
            }
        }

        for (slot in (Slot.RowTwoSlotFive rect Slot.RowFourSlotSeven).enumerate(UIDimensions.SIX_X_NINE)) {
            storageSlot(Slot.fromRaw(slot), { stack -> stack.macrocosm is PowerStone }, { data, stack ->
                stones.add(stack)
                data.instance.reload()
            }, { data, stack ->
                stones.remove(stack)
                data.instance.reload()
            })
        }
    }
}
