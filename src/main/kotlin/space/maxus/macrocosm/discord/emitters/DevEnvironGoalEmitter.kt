package space.maxus.macrocosm.discord.emitters

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.Channel
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.discord.DiscordEmitter
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.stripTags

class DevEnvironGoalEmitter(role: Role, channel: Channel): DiscordEmitter<DevEnvironGoalEmitter.Context>(
    "Goal Achieved",
    role,
    channel
) {
    data class Context(val player: MacrocosmPlayer, val goal: String)

    override fun handle(subject: Context, bot: JDA) {
        dedicatedChannel.sendMessageEmbeds(Discord.embed {
            setTitle("**New Goal Achieved!**")
            setColor(Discord.COLOR_MACROCOSM)
            setAuthor(
                "${subject.player.rank.format.str().stripTags()} ${subject.player.paper?.name}",
                null,
                Discord.playerAvatar(subject.player)
            )
            setThumbnail(Discord.playerAvatar(subject.player))

            addField(
                "Goal: ",
                "`${subject.goal}`",
                false
            )
        }).setContent(role.asMention).queue()
    }
}
