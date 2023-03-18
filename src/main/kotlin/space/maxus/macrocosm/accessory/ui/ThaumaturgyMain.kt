package space.maxus.macrocosm.accessory.ui

import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.accessory.rarityToMp
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi

fun thaumaturgyUi(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("thaumaturgy", UIDimensions.SIX_X_NINE) {
    title = "Accessory Bag Thaumaturgy"
    pageLazy {
        val totalMp = player.accessoryBag.magicPower
        background()

        switchUi(
            Slot.RowSixSlotThree,
            { player.accessoryBag.ui(player) },
            ItemValue.placeholderHeadDesc(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMwYzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0=",
                "<green>Accessory Bag Shortcut",
                "Quickly access your accessory",
                "bag from right here!",
                "",
                "<yellow>Click to open!"
            )
        )
        placeholder(
            Slot.RowSixSlotFour,
            ItemValue.placeholderDescripted(
                Material.FILLED_MAP,
                "<green>Accessories Breakdown",
                "<dark_gray>From your bag",
                "",
                *Rarity.values().map { rarity ->
                    val mp = rarityToMp[rarity]!!
                    val owned = player.accessoryBag.accessories.count { it.rarity == rarity }
                    val product = mp * owned
                    "<gold>$mp MP <gray>x <${rarity.color.asHexString()}>$owned Accs. <gray> = <gold>$product MP"
                }.toTypedArray(),
                "",
                "Total: <gold>${totalMp} Magic Power"
            )
        )
        close()

        switchUi(
            Slot.RowSixSlotSix,
            { learnPowerUi(player, mutableListOf()) },
            ItemValue.placeholderHeadDesc(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFlMWY2MTYyZGI0MjI0NTYzOTYwOWY3MjhhNGUxMzRlZDdiZDdkZTNjMTVhNzc5MmQyMTlhNmUyYTlkYiJ9fX0=",
                "<green>Learn Power From Stones",
                "Combine <green>9x<gray> identical <blue>Power",
                "<blue>Stones<gray> that you may find",
                "across <#${Discord.COLOR_MACROCOSM.toString(16)}>Macrocosm<gray> to",
                "permanently unlock their",
                "<green>power<gray>.",
                "",
                "<yellow>Click to combine stones!"
            )
        )

        compound(
            Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight,
            player.memory.knownPowers.map { Registry.ACCESSORY_POWER.find(it) },
            {
                it.thaumaturgyPlaceholder(player, totalMp, player.accessoryBag.power == it.id)
            },
            { e, power ->
                if (player.accessoryBag.power != power.id) {
                    // not selected, selecting
                    player.accessoryBag.power = power.id

                    sound(Sound.BLOCK_LEVER_CLICK) {
                        playFor(e.paper)
                    }
                    var counter = 0
                    task(sync = false, period = 2L) {
                        counter++
                        sound(Sound.ENTITY_CHICKEN_EGG) {
                            pitch = 1 + (counter / 10f)
                            playFor(e.paper)
                        }
                        if (counter >= 5)
                            it.cancel()
                    }

                    e.player.sendMessage("<yellow>You selected the <green>${power.name}<yellow> power for your <green>Accessory Bag<yellow>!")

                    e.instance.reload()
                }
            })
    }
}
