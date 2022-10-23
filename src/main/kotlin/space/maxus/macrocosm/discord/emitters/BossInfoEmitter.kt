package space.maxus.macrocosm.discord.emitters

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Role
import space.maxus.macrocosm.discord.DiscordEmitter

/**
 * An emitter that posts message once a boss is being summoned/defeated
 */
class BossInfoEmitter(role: Role, channel: Channel) :
    DiscordEmitter<BossInfoEmitter.Context>("Boss Information", role, channel) {
    /**
     * A null context, this will be implemented later
     */
    data class Context(val filler: Unit)

    override fun handle(subject: Context, bot: JDA) {
        /* no-op */
    }
}
