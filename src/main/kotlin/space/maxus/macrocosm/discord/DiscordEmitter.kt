package space.maxus.macrocosm.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Channel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.util.runCatchingReporting

abstract class DiscordEmitter<S>(val name: String, val role: Role, dedicatedChannel: Channel) {
    val dedicatedChannel = dedicatedChannel as TextChannel
    companion object {
        internal val SHARED_EMITTER_POOL = Threading.newFixedPool(16)
    }

    abstract fun handle(subject: S, bot: JDA)

    fun post(subject: S) {
        SHARED_EMITTER_POOL.execute {
            runCatchingReporting {
                handle(subject, Discord.bot)
            }
        }
    }

    fun subscribe(user: Member) {
        user.guild.addRoleToMember(user, role).queue()
    }

    fun unsubscribe(user: Member) {
        user.guild.removeRoleFromMember(user, role).queue()
    }
}
