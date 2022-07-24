package space.maxus.macrocosm.loot

import net.axay.kspigot.extensions.broadcast
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.TitlePart
import org.bukkit.Sound
import org.bukkit.entity.Player
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import kotlin.math.roundToInt

open class DropRarity(
    val broadcast: Boolean,
    val rarity: Int,
    val greet: Boolean = false,
    val name: String? = null,
    val odds: String = "<green>Guaranteed"
) {
    companion object {
        val COMMON = DropRarity(false, 0)
        val RARE = DropRarity(true, 1, name = "<gold>RARE", odds = "<aqua>Occasional")
        val VERY_RARE = DropRarity(true, 2, greet = true, "<blue>VERY RARE", "<blue>Rare")
        val SUPER_RARE = DropRarity(true, 3, greet = true, "<dark_purple>SUPER RARE", "<dark_purple>Extraordinary")
        val CRAZY_RARE = DropRarity(true, 4, greet = true, "<light_purple>CRAZY RARE", "<light_purple>Pray RNGesus")
        val INSANE = DropRarity(true, 5, greet = true, "<red>INSANE", "<red>RNGesus Incarnate")
        val UNBELIEVABLE = DropRarity(true, 6, greet = true, "<rainbow>UNBELIEVABLE", "<rainbow>Unbelievable")
    }

    data class Pet(val inner: DropRarity) :
        DropRarity(inner.broadcast, inner.rarity, inner.greet, "<gold>PET", inner.odds)

    fun announceEntityDrop(player: Player, item: MacrocosmItem, artificial: Boolean = false) {
        if (!broadcast)
            return
        val mf = player.macrocosm!!.stats()!!.magicFind
        val message = text(
            "<bold>$name DROP!</bold> ${
                MiniMessage.miniMessage().serialize(item.buildName())
            } ${if (artificial) "<light_purple>(<dark_purple>RNGesus Meter Reward<light_purple>)" else "<aqua>(${mf.roundToInt()} ${Statistic.MAGIC_FIND.display})"}"
        )
        player.sendMessage(message)
        if (greet) {
            for (i in 0..5) {
                taskRunLater(i * 2L) {
                    sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                        pitch = 1 + i * 0.2f
                        playFor(player)
                    }
                }
            }
        }
        if (this == UNBELIEVABLE || this == INSANE) {
            broadcast(
                text(
                    "<red><bold>WOW</bold><gold> ${
                        MiniMessage.miniMessage().serialize(player.displayName())
                    }<gold> has found ${MiniMessage.miniMessage().serialize(item.buildName())}<gold>!"
                )
            )
            player.sendTitlePart(TitlePart.TITLE, text("<bold>${this.name} DROP!"))
            player.sendTitlePart(TitlePart.SUBTITLE, item.buildName())
            taskRunLater(18L) {
                sound(Sound.ENTITY_ENDER_DRAGON_GROWL) {
                    pitch = 1.4f
                    volume = 2f
                    playFor(player)
                }
            }
        }
    }
}
