package space.maxus.macrocosm.entity.loot

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
import space.maxus.macrocosm.text.comp
import kotlin.math.roundToInt

class DropRarity private constructor(val broadcast: Boolean, val greet: Boolean = false, val name: String? = null) {
    companion object {
        val COMMON = DropRarity(false)
        val RARE = DropRarity(true, name = "<gold>RARE")
        val VERY_RARE = DropRarity(true, greet = true, "<blue>VERY RARE")
        val SUPER_RARE = DropRarity(true, greet = true, "<dark_purple>SUPER RARE")
        val CRAZY_RARE = DropRarity(true, greet = true, "<light_purple>CRAZY RARE")
        val INSANE = DropRarity(true, greet = true, "<red>INSANE")
        val UNBELIEVABLE = DropRarity(true, greet = true, "<#2ACE7C>UNBELIEVABLE")
    }

    fun announceEntityDrop(player: Player, item: MacrocosmItem) {
        if (!broadcast)
            return
        val mf = player.macrocosm!!.stats()!!.magicFind
        val message = comp(
            "<bold>$name DROP!</bold> ${
                MiniMessage.miniMessage().serialize(item.name.color(item.rarity.color))
            } <aqua>(${mf.roundToInt()} ${Statistic.MAGIC_FIND.display})"
        )
        player.sendMessage(message)
        if (greet) {
            for (i in 0..5) {
                taskRunLater(i * 3L) {
                    sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                        pitch = 1 + i * 0.2f
                        playFor(player)
                    }
                }
            }
        }
        if (this == UNBELIEVABLE) {
            broadcast(
                comp(
                    "<red><bold>WOW</bold><gold> ${
                        MiniMessage.miniMessage().serialize(player.displayName())
                    }<gold> has found ${MiniMessage.miniMessage().serialize(item.name.color(item.rarity.color))}<gold>!"
                )
            )
            player.sendTitlePart(TitlePart.TITLE, comp("<#2ACE7C><bold>UNBELIEVABLE DROP!"))
            player.sendTitlePart(TitlePart.SUBTITLE, item.name.color(item.rarity.color))
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
