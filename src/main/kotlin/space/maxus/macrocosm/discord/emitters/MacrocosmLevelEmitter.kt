package space.maxus.macrocosm.discord.emitters

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.Channel
import space.maxus.macrocosm.discord.DiscordEmitter

/**
 * An emitter that posts a message once a player reaches a high Macrocosm Level
 */
class MacrocosmLevelEmitter(role: Role, channel: Channel) :
    DiscordEmitter<MacrocosmLevelEmitter.Context>("Macrocosm High Level Up", role, channel) {
    /**
     * A null context, will be implemented later
     */
    data class Context(val filler: Unit)

    override fun handle(subject: Context, bot: JDA) {
        /* no-op */
    }
}
