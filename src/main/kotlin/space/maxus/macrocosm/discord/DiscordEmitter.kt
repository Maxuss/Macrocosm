package space.maxus.macrocosm.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.exceptions.macrocosm
import space.maxus.macrocosm.util.runCatchingReporting

/**
 * An emitter hook that can send messages to discord when certain events happen
 */
abstract class DiscordEmitter<S>(
    /**
     * Name of this emitter
     */
    val name: String,
    /**
     * Role that will be pinged by this emitter
     */
    val role: Role,
    /**
     * A dedicated channel to which the messages will be posted
     */
    dedicatedChannel: Channel
) {
    /**
     * A dedicated channel to which the messages will be posted
     */
    val dedicatedChannel = dedicatedChannel as TextChannel

    companion object {
        internal val SHARED_EMITTER_POOL = Threading.newFixedPool(16)
    }

    /**
     * Handles the event, sending es
     */
    abstract fun handle(subject: S, bot: JDA)

    /**
     * Posts an event of type [S] to this emitter
     */
    fun post(subject: S) {
        if(!Discord.enabled)
            return
        SHARED_EMITTER_POOL.execute {
            runCatchingReporting {
                handle(subject, Discord.bot)
            }.onFailure { err ->
                val mc = err.macrocosm
                dedicatedChannel.sendMessageEmbeds(mc.embed).queue()
            }
        }
    }

    /**
     * Subscribes a member to this emitter
     */
    fun subscribe(user: Member) {
        user.guild.addRoleToMember(user, role).queue()
    }

    /**
     * Unsubscribes a member from this emitter
     */
    fun unsubscribe(user: Member) {
        user.guild.removeRoleFromMember(user, role).queue()
    }
}
