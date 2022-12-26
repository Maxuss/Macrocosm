package space.maxus.macrocosm.discord.emitters

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.Channel
import org.bukkit.entity.Player
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.discord.DiscordEmitter
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.Drop
import space.maxus.macrocosm.loot.DropRarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.stripTags
import java.text.DecimalFormat


/**
 * An emitter that posts message once player gets a really rare drop ([DropRarity.INSANE] | [DropRarity.UNBELIEVABLE])
 */
class RareDropEmitter(role: Role, channel: Channel) :
    DiscordEmitter<RareDropEmitter.Context>("Rare Drops", role, channel) {
    /**
     * Context for the rare drop emitter
     *
     * @property player player that got the drop
     * @property paper paper mirror of the player
     * @property drop drop the player has got
     * @property item item that the player has got
     */
    data class Context(val player: MacrocosmPlayer, val paper: Player, val drop: Drop, val item: MacrocosmItem)

    override fun handle(subject: Context, bot: JDA) {
        dedicatedChannel.sendMessageEmbeds(Discord.embed {
            setColor(Discord.COLOR_MACROCOSM)
            setTitle("**Rare Drop!**")
            setAuthor(
                "${subject.player.rank.format.str().stripTags()} ${subject.paper.name}",
                null,
                Discord.playerAvatar(subject.player)
            )
            setImage(Discord.itemImage(subject.item))

            addField(
                "**${subject.drop.rarity.name?.stripTags()} DROP**",
                "${
                    subject.player.rank.format.str().stripTags()
                } ${subject.paper.name} has just found a rare drop: `${subject.item.buildName().str().stripTags()}`",
                false
            )
            val ch = subject.drop.chance * 100f
            addField(
                "**Chance**",
                "**${if (ch > 1) DecimalFormat("###.00").format(ch) else DecimalFormat("###.0000").format(ch)}%**",
                true
            )
            addField(
                "**Magic Find**",
                "${Statistic.MAGIC_FIND.specialChar} **${Formatting.withCommas((subject.player.stats()?.magicFind ?: .0f).toBigDecimal())}**",
                true
            )
        }).setContent(role.asMention).queue()
    }
}
