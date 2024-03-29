package space.maxus.macrocosm.accessory.ui

import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.metrics.MacrocosmMetrics
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.banking.Transaction
import space.maxus.macrocosm.players.banking.transact
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.giveOrDrop

val jacobusCoinsSpent by lazy { MacrocosmMetrics.counter("jacobus_coins_spent", "Coins spent on Jacobus upgrades") }

fun jacobusUi(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("jacobus", UIDimensions.FOUR_X_NINE) {
    title = "Accessory Bag Upgrades"

    pageLazy {
        background()

        val bag = player.accessoryBag

        val nextAmount = slotAmounts[bag.jacobusSlots % 3]
        val nextPrice = when (bag.jacobusSlots) {
            in 42..198 -> 20_000_000
            in 22..42 -> 10_000_000
            in 15..22 -> 8_000_000
            in 10..15 -> 5_000_000
            in 4..10 -> 3_000_000
            else -> 1_500_000
        }

        close(Slot.RowLastSlotFive)

        button(
            Slot.RowTwoSlotThree, ItemValue.placeholderHeadDesc(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2I2NDhiOWE0NGUyODBiY2RmMjVmNGE2NmE5N2JkNWMzMzU0MmU1ZTgyNDE1ZTE1YjQ3NWM2Yjk5OWI4ZDYzNSJ9fX0=",
                "<green>Accessory Bag Upgrades",
                "Increases the size of your",
                "accessory bag.",
                "",
                "Redstone collection: <red>+${bag.redstoneCollSlots} Slots",
                "Mithril collection: <aqua>+${bag.mithrilCollSlots} Slots",
                "Jacobus: <gold>+${bag.jacobusSlots} Slots",
                "Total: <green>${bag.capacity} Slots",
                "",
                "Buying: <gold>+${nextAmount} Slots",
                "Cost: <gold>${Formatting.withCommas(nextPrice.toBigDecimal(), true)} Coins",
                "",
                "<yellow>Click to buy!"
            )
        ) { e ->
            if (player.purse >= nextPrice.toBigDecimal()) {
                val amount = transact(nextPrice.toBigDecimal(), player.ref, Transaction.Kind.OUTGOING)
                jacobusCoinsSpent.inc(amount.toDouble())

                player.purse -= amount
                bag.jacobusSlots += nextAmount
                bag.capacity += nextAmount
                if (bag.jacobusSlots == 8) {
                    // award a register on 5th purchase
                    e.paper.giveOrDrop(Registry.ITEM.find(id("jacobus_register")).build(player)!!)
                    player.sendMessage("<green>You have been awarded by <gold>Jacqueefis<green> with a <gold>Jacqueefis Register<green>!")
                }
                var count = 0
                task(period = 2L, sync = false) {
                    count += 1
                    sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                        volume = 3f
                        pitch = 1 + count * .1f
                        playAt(e.paper.location)
                    }
                    if (count >= 6) {
                        it.cancel()
                        sound(Sound.BLOCK_ANVIL_USE) {
                            pitch = 2f
                            volume = 3f
                            playAt(e.paper.location)
                        }
                        return@task
                    }
                }
                player.sendMessage(
                    "<yellow>Upgraded accessory bag size by <gold>+${nextAmount} Slots<green> for <gold>${
                        Formatting.withCommas(
                            nextPrice.toBigDecimal(),
                            true
                        )
                    } Coins<yellow>!"
                )
                e.instance.reload()
            } else {
                sound(Sound.ENTITY_VILLAGER_NO) {
                    volume = 2f
                    pitch = 2f
                    playFor(e.paper)
                }
                player.sendMessage("<red>Not enough coins!")
            }
        }

        switchUi(
            Slot.RowTwoSlotSeven,
            {
                player.accessoryBag.ui(player)
            },
            ItemValue.placeholderHeadDesc(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMwYzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0=",
                "<green>Accessory Bag Shortcut",
                "Quickly access your accessory",
                "bag from right here!",
                "",
                "<yellow>Click to open!"
            )
        )
    }
}

val slotAmounts = listOf(2, 2, 1)
val slotPrices = listOf(1_500_000, 2_000_000, 3_000_000)
