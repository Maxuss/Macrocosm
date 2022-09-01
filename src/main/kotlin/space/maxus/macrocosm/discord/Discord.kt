package space.maxus.macrocosm.discord

import com.google.common.collect.HashBiMap
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.discordBotToken
import space.maxus.macrocosm.logger
import space.maxus.macrocosm.util.fromJson
import space.maxus.macrocosm.util.general.ConditionalValueCallback
import space.maxus.macrocosm.util.toJson
import java.util.*
import java.util.concurrent.ExecutorService

object Discord: ListenerAdapter() {
    private val authenticationBridge: HashBiMap<UUID, Pair<String, String>> = HashBiMap.create()
    private val authenticated: HashBiMap<UUID, Long> = HashBiMap.create()

    fun hasBegunAuth(id: UUID): ConditionalValueCallback<String> {
        return if(authenticationBridge.containsKey(id)) {
            ConditionalValueCallback.success(authenticationBridge[id]!!.second)
        } else {
            ConditionalValueCallback.fail()
        }
    }
    fun hasAuthenticated(id: UUID) = authenticated.containsKey(id)

    fun step1Auth(id: UUID, user: String, key: String) {
        authenticationBridge[id] = Pair(user, key)
    }

    fun readSelf() {
        Accessor.readIfOpen("discord_auth.json").then {
            authenticated.putAll(fromJson<Map<UUID, Long>>(it)!!)
        }.call()
    }

    fun storeSelf() {
        Accessor.overwrite("discord_auth.json", toJson(authenticated.toMap()))
        bot.shutdown()
    }

    private val commandPool: ExecutorService = Threading.newFixedPool(8)
    private lateinit var bot: JDA

    fun setupBot() {
        Threading.runAsyncRaw {
            bot = JDABuilder.createLight(discordBotToken).addEventListeners(this).setActivity(Activity.playing("Macrocosm Server, type /info")).build()

            val commands = bot.updateCommands()

            commands.addCommands(
                Commands.slash("ping", "Gets the ping of Macrocosm Bot").setGuildOnly(true),
                Commands.slash("info", "Gets general info about Macrocosm")
                    .addOption(OptionType.STRING, "category", "Category for the bot to give info on", true, true),
                Commands.slash("auth", "Links your Minecraft account with this Discord account.")
                    .addOption(OptionType.STRING, "key", "Your unique authentication key", true, false)
            ).queue()
        }
    }

    override fun onCommandAutoCompleteInteraction(e: CommandAutoCompleteInteractionEvent) {
        when(e.name) {
            "info" -> e.replyChoiceStrings("server", "bot").queue()
        }
    }

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        commandPool.execute {
            when (e.name) {
                "ping" -> pingCommand(e)
                "info" -> infoCommand(e)
                "auth" -> authCommand(e)
                else -> {
                    logger.warning("Invalid command used: ${e.name}")
                    // invalid command
                }
            }
        }
    }

    private fun authCommand(e: SlashCommandInteractionEvent) {
        val key = e.getOption("key")?.asString ?: return e.reply("Key not provided!").setEphemeral(true).queue()
        val fitting = authenticationBridge.entries.firstOrNull { (_, p) -> p.second == key }
        if(fitting == null) {
            return e.replyEmbeds(buildErrorEmbed("Not Found", "You have not begun authentication process yet! Run `/discordauth <discord username>` to start!")).setEphemeral(true).queue()
        } else {
            val (username, _) = fitting.value
            val currentUserName = "${e.user.name}#${e.user.discriminator}"
            if(username != currentUserName) {
                return e.replyEmbeds(buildErrorEmbed("Invalid User", "This authentication process was started by a different user!")).setEphemeral(true).queue()
            }
            authenticationBridge.remove(fitting.key)
            authenticated[fitting.key] = e.user.idLong
            e.replyEmbeds(buildSuccessEmbed("Authentication Successful!", "Account `${Bukkit.getOfflinePlayer(fitting.key).name}` was linked with Discord account ${e.user.asMention}!")).queue()
        }
    }

    private fun infoCommand(e: SlashCommandInteractionEvent) {
        val category = e.getOption("category")?.asString ?: return e.reply("Category missing!").setEphemeral(true).queue()
        when(category) {
            "server" -> e.replyEmbeds(EmbedBuilder().addField("Test a", "A", true).setColor(NamedTextColor.LIGHT_PURPLE.value()).build()).queue()
            "bot" -> e.replyEmbeds(EmbedBuilder().addField("Test b", "B", false).setColor(NamedTextColor.DARK_PURPLE.value()).build()).queue()
            else -> e.reply("Invalid category!").setEphemeral(true).queue()
        }
    }

    private fun pingCommand(e: SlashCommandInteractionEvent) {
        val now = System.currentTimeMillis()
        e.reply("Pong!").setEphemeral(true).flatMap { e.hook.editOriginal("Bot Ping: **${System.currentTimeMillis() - now}ms**") }.queue()
    }

    private fun buildErrorEmbed(error: String, message: String): MessageEmbed {
        return EmbedBuilder().setTitle("**Error**").addField("**$error**", message, false).setColor(NamedTextColor.RED.value()).build()
    }

    private fun buildSuccessEmbed(title: String, message: String): MessageEmbed {
        return EmbedBuilder().setTitle("**Success**").addField("**$title**", message, false).setColor(Macrocosm.macrocosmColor.value()).build()
    }
}
