package space.maxus.macrocosm.discord.emitters

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Role
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.discord.Discord
import space.maxus.macrocosm.discord.DiscordEmitter
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.stripTags

class HighSkillEmitter(role: Role, channel: Channel): DiscordEmitter<HighSkillEmitter.Context>("High Skill Level Up", role, channel) {
    data class Context(val skill: SkillType, val level: Int, val player: MacrocosmPlayer)

    override fun handle(subject: Context, bot: JDA) {
        dedicatedChannel.sendMessageEmbeds(Discord.embed {
            setTitle("**High Skill Level Up!**")
            setColor(Discord.COLOR_MACROCOSM)
            setAuthor("${subject.player.rank.format.str().stripTags()} ${subject.player.paper?.name}", null, Discord.playerAvatar(subject.player))
            setThumbnail(Discord.playerAvatar(subject.player))

            addField("**${subject.skill.emoji} ${subject.skill.inst.name} Level _${subject.level}_ Reached**", "${Discord.getAuthenticatedOrNull(subject.player)?.asMention ?: "${subject.player.rank.format.str().stripTags()} ${subject.player.paper?.name}"} is now `${subject.skill.profession} LVL ${subject.level}`!", false)
            addField("**Total Experience**", "**${Formatting.withCommas(subject.skill.inst.table.totalExpForLevel(subject.level).toBigDecimal())} EXP**${if(subject.skill.maxLevel == subject.level) "MAX LEVEL!" else ""}", false)
        }).setContent(role.asMention).queue()
    }
}
