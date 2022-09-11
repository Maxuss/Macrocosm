package space.maxus.macrocosm.discord.emitters

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Role
import space.maxus.macrocosm.discord.DiscordEmitter

class BossInfoEmitter(role: Role, channel: Channel) :
    DiscordEmitter<BossInfoEmitter.Context>("Boss Information", role, channel) {
    data class Context(val filler: Unit)

    override fun handle(subject: Context, bot: JDA) {
        /* no-op */
    }
}
