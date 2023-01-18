package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.damage.truncateBigNumber
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.stripTags
import kotlin.math.roundToInt

val slayerLevelBuff = arrayOf(1.0, 1.0, 1.0, .95, .95, .9, .85, .85, .8)
fun rngMeterButton(slayer: SlayerLevel, slayerType: SlayerType): ItemStack {
    return itemStack(Material.PAINTING) {
        meta {
            displayName(text("<light_purple>RNG Meter").noitalic())

            val lore = mutableListOf(
                "<dark_gray>${slayerType.slayer.name.stripTags()}",
                "",
                "Your <light_purple>RNG Meter<gray> fills with",
                "<light_purple>${slayerType.slayer.entityKind} Slayer XP<gray> every",
                "time you defeat ${slayerType.slayer.name} III<gray> or higher!",
                "",
                "When your meter is full, your",
                "selected drop will be guaranteed",
                "to drop the next time you defeat",
                "the boss!",
                ""
            )

            if (slayer.rng[slayerType]!!.selectedRngDrop == -1) {
                lore.addAll(
                    listOf(
                        "<red>You don't have an RNG drop",
                        "<red>selected. Choose one to start",
                        "<red>progressing towards it!"
                    )
                )
            } else {
                val drop = slayerType.slayer.drops[slayer.rng[slayerType]!!.selectedRngDrop]
                val item = Registry.ITEM.findOrNull(drop.drop.item) ?: return ItemValue.NULL.item.build(null)!!
                lore.addAll(
                    listOf(
                        "Selected Drop",
                        MiniMessage.miniMessage().serialize(item.buildName())
                    )
                )
                lore.add("")
                val expToDrop = (((1 / drop.drop.chance) * 900) * slayerLevelBuff[slayer.level]).roundToInt()
                val ratio = slayer.rng[slayerType]!!.expAccumulated / expToDrop
                val progress = ratio * 100
                lore.add("Progress: <light_purple>${Formatting.withCommas(progress.toBigDecimal())}<dark_purple>%")
                val barCount = (ratio * 25).roundToInt().coerceIn(0..25)
                lore.add(
                    "<light_purple>${"-".repeat(barCount)}<white>${"-".repeat(25 - barCount)} <light_purple>${
                        Formatting.withCommas(
                            slayer.rng[slayerType]!!.expAccumulated.toBigDecimal(),
                            true
                        )
                    }<dark_purple>/<light_purple>${truncateBigNumber(expToDrop.toFloat(), false)}"
                )
            }

            lore.add("")

            lore.add(
                "Stored Slayer XP: <light_purple>${
                    Formatting.withCommas(
                        slayer.rng[slayerType]!!.expAccumulated.toBigDecimal(),
                        true
                    )
                }"
            )

            lore(lore.map { text("<gray>$it").noitalic() })
        }
    }
}

fun rngMeter(player: MacrocosmPlayer, slayer: SlayerLevel, slayerType: SlayerType): GUI<ForInventorySixByNine> =
    kSpigotGUI(GUIType.SIX_BY_NINE) {
        defaultPage = 0
        title = text("${slayerType.slayer.name.stripTags()} RNG Meter")

        page(0) {
            placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
            placeholder(Slots.RowSixSlotFive, rngMeterButton(slayer, slayerType))
            button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) {
                it.player.closeInventory()
            }

            val compound = createCompound<Pair<Int, SlayerDrop>>({ (dropIndex, drop) ->
                if (drop.requiredLevel > slayer.level) {
                    return@createCompound ItemValue.placeholderDescripted(
                        Material.COAL_BLOCK,
                        "<red>???",
                        "Requires ${slayerType.slayer.name} ${drop.requiredLevel}<gray>!"
                    )
                }
                val oldChance = drop.drop.chance
                val expToDrop = (((1 / oldChance) * 900) * slayerLevelBuff[slayer.level]).roundToInt()
                val newChance = oldChance + (slayer.rng[slayerType]!!.expAccumulated / expToDrop) * .03
                val item = buildDropItem(
                    player,
                    slayer,
                    drop,
                    if (dropIndex == slayer.rng[slayerType]!!.selectedRngDrop) newChance.toFloat() else -1f
                )
                item.meta {
                    val lore = lore()!!.toMutableList()

                    lore.add(Component.empty())

                    if (slayer.rng[slayerType]!!.selectedRngDrop == dropIndex) {
                        val ratio = slayer.rng[slayerType]!!.expAccumulated / expToDrop
                        val progress = ratio * 100
                        lore.add(text("<gray>Progress: <light_purple>${Formatting.withCommas(progress.toBigDecimal())}<dark_purple>%").noitalic())
                        val barCount = (ratio * 25).roundToInt().coerceIn(0..25)
                        lore.add(
                            text(
                                "<light_purple>${"-".repeat(barCount)}<white>${"-".repeat(25 - barCount)} <light_purple>${
                                    Formatting.withCommas(
                                        slayer.rng[slayerType]!!.expAccumulated.toBigDecimal(),
                                        true
                                    )
                                }<dark_purple>/<light_purple>${truncateBigNumber(expToDrop.toFloat(), false)}"
                            ).noitalic()
                        )
                        lore.add(Component.empty())
                        lore.addAll(listOf(
                            "Filling the meter increases the",
                            "drop chance of this item.",
                            "Reaching <green>100%<gray> will guarantee",
                            "it to drop!",
                            "",
                            "<green>SELECTED"
                        ).map { text("<gray>$it").noitalic() })
                    } else {
                        lore.add(
                            text(
                                "<gray>Slayer XP: <light_purple>${
                                    Formatting.withCommas(
                                        slayer.rng[slayerType]!!.expAccumulated.toBigDecimal(),
                                        true
                                    )
                                }<dark_purple>/<light_purple>${Formatting.withCommas(expToDrop.toBigDecimal(), true)}"
                            ).noitalic()
                        )
                        lore.add(Component.empty())
                        lore.add(text("<yellow>Click to select!").noitalic())
                    }

                    lore(lore)
                }
                item
            }) { e, (dropIndex, drop) ->
                e.bukkitEvent.isCancelled = true
                if (slayer.rng[slayerType]!!.selectedRngDrop != dropIndex && drop.requiredLevel <= slayer.level) {
                    slayer.rng[slayerType]!!.selectedRngDrop = dropIndex
                    sound(Sound.BLOCK_NOTE_BLOCK_HARP) {
                        volume = 2f
                        playFor(e.player)
                    }
                    e.player.closeInventory()
                    e.player.openGUI(rngMeter(player, slayer, slayerType))
                }
            }
            compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFiveSlotEight, compound)
            compound.addContent(
                slayerType.slayer.drops.indices.associateWith { slayerType.slayer.drops[it] }.toList()
                    .filter { it.second.drop.chance < 1 })
        }
    }
