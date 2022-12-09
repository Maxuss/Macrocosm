package space.maxus.macrocosm.discord

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.gson.JsonObject
import io.papermc.paper.event.player.AsyncChatEvent
import net.axay.kspigot.extensions.server
import net.axay.kspigot.runnables.taskRunLater
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.*
import space.maxus.macrocosm.api.APIPermission
import space.maxus.macrocosm.api.KeyManager
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.bazaar.*
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.discord.emitters.BossInfoEmitter
import space.maxus.macrocosm.discord.emitters.HighSkillEmitter
import space.maxus.macrocosm.discord.emitters.MacrocosmLevelEmitter
import space.maxus.macrocosm.discord.emitters.RareDropEmitter
import space.maxus.macrocosm.exceptions.macrocosm
import space.maxus.macrocosm.graphics.ItemRenderBuffer
import space.maxus.macrocosm.graphics.StackRenderer
import space.maxus.macrocosm.item.*
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.PlayerEquipment
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.*
import space.maxus.macrocosm.util.data.SemanticVersion
import space.maxus.macrocosm.util.general.ConditionalValueCallback
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.metrics.report
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.io.path.deleteIfExists

/**
 * A global object that is used for communications with discord bot
 */
@Suppress("UNUSED_PARAMETER")
object Discord : ListenerAdapter() {
    /**
     * A connection loop for messaging between discord and server
     */
    object ConnectionLoop : ListenerAdapter(), Listener {
        private val connectionPool: ExecutorService = Threading.newFixedPool(16)
        private var client: JDAWebhookClient? = null

        /**
         * Initializes a connection loop.
         *
         * This function is **NOT Thread Safe**, so it must be called
         * in synchronous environment
         */
        fun init() {
            client = WebhookClientBuilder(webhookLink!!).setThreadFactory(
                ThreadFactoryBuilder()
                    .setNameFormat("Webhook Thread").setDaemon(true).build()
            ).setWait(true).buildJDA()
            Macrocosm.logger.info("Webhook connection initialized!")
        }

        private fun trySendMessage(msgStr: String, player: Player) {
            val builder = WebhookMessageBuilder()
            if (authenticated.containsKey(player.uniqueId)) {
                val userId = authenticated[player.uniqueId]!!
                val user = bot.getUserById(userId) ?: bot.retrieveUserById(userId).submit().get()
                builder.setUsername(user.name).setAvatarUrl(user.avatarUrl)
                    .setAllowedMentions(AllowedMentions().withParseUsers(true).withParseRoles(true))
            } else {
                builder.setUsername(player.name)
                    .setAvatarUrl("https://crafatar.com/avatars/${player.uniqueId}?overlay=true")
                    .setAllowedMentions(AllowedMentions.none())
            }
            val built = builder.setContent(msgStr).build()
            client?.send(built)

        }

        @EventHandler(priority = EventPriority.LOWEST)
        fun onMessage(e: AsyncChatEvent) {
            if (communicationChannel != 0L && client != null && ::bot.isInitialized) {
                connectionPool.execute {
                    val msgStr = e.originalMessage().str().stripTags()
                    trySendMessage(msgStr, e.player)
                }
            }
        }

        override fun onMessageReceived(event: MessageReceivedEvent) {
            if (event.channel.idLong == communicationChannel) {
                if (event.message.author.idLong == 1014930457353265255 || event.message.author.idLong == 1014935882853257306) {
                    // ignore the bot itself
                    return
                }
                connectionPool.execute {
                    server.broadcast(
                        if (!event.author.isBot) {
                            val id =
                                authenticated.entries.firstOrNull { (_, userId) -> userId == event.author.idLong }?.key
                            val player = if (id != null) MacrocosmPlayer.loadPlayer(id) else null
                            if (id != null && player != null) {
                                val offline = Bukkit.getOfflinePlayer(id)
                                player.rank.format(offline.name ?: event.author.name, event.message.contentStripped)
                            } else
                                text("<gray>${event.author.name}: <white>${event.message.contentStripped}")
                        } else {
                            text(
                                "<blue>[BOT] ${event.author.name}<gray>: <white>${event.message.contentStripped} ${
                                    event.message.embeds.joinToString(
                                        separator = ""
                                    ) { flattenEmbed(it) }
                                }"
                            )
                        }
                    )
                }
            }
        }

        private fun flattenEmbed(embed: MessageEmbed): String {
            val color = TextColor.color(embed.colorRaw).asHexString()
            return "<br><bold><$color>||<white> ${embed.title}</bold>${
                embed.fields.joinToString(separator = "<br><$color><bold>||</bold></$color> ") {
                    "<br><$color><bold>||</bold></$color><white> ${it.name}<br><$color><bold>||</bold></$color> <white>${
                        it.value?.reduceToList(
                            35
                        )?.joinToString(separator = "<br><$color><bold>||</bold></$color><white> ")
                    }"
                }
            }"
        }
    }

    private val authenticationBridge: HashMap<UUID, Pair<String, String>> = hashMapOf()
    private val authenticated: HashMap<UUID, Long> = hashMapOf()

    private val commandPool: ExecutorService = Threading.newFixedPool(8)

    /**
     * The JDA bot instance
     */
    lateinit var bot: JDA

    /**
     * Text channel for general communications
     */
    var commTextChannel: TextChannel? = null

    /**
     * Gets whether the discord bot is enabled
     */
    val enabled: Boolean
        get() {
            return Macrocosm.isOnline && ::bot.isInitialized
        }
    private var communicationChannel: Long? = null
    private var webhookLink: String? = null
    private val bazaarSellCache: Cache<Identifier, ListIterator<BazaarSellOrder>> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()
    private val bazaarBuyCache: Cache<Identifier, ListIterator<BazaarBuyOrder>> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()
    private val bazaarUserCache: Cache<UUID, ListIterator<BazaarOrder>> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()

    /**
     * RGB red color preferred for error embeds
     */
    const val COLOR_RED = 0xBF0000

    /**
     * RGB macrocosm specific blueish-purple color preferred for success embeds
     */
    const val COLOR_MACROCOSM = 0x4A26BB

    private val EMBED_AUTH_REQUIRED = embed {
        setColor(COLOR_RED)
        setTitle("**Not Authenticated**")
        addField("**Authentication Required**", "This command requires authentication to work!", false)
        addField(
            "**How to Authenticate**",
            "**1.** Run `/discordauth <discord username>` on the server to begin authentication process and get *auth token*.\n**2.** Run `/auth <token>` in the bot to link your accounts",
            false
        )
    }

    /**
     * Checks if the authentication process has begun for player with [id].
     *
     * Returns false if the authentication has already happened OR player did not start authentication.
     */
    fun hasBegunAuth(id: UUID): ConditionalValueCallback<String> {
        return if (authenticationBridge.containsKey(id)) {
            ConditionalValueCallback.success(authenticationBridge[id]!!.second)
        } else {
            ConditionalValueCallback.fail()
        }
    }

    /**
     * Checks if the player with [id] has already authenticated.
     */
    fun hasAuthenticated(id: UUID) = authenticated.containsKey(id)

    /**
     * Performs step 1 of authentication.
     *
     * @param id UUID of player to authenticate
     * @param user expected discord username of the player
     * @param key authentication key of the player
     */
    fun step1Auth(id: UUID, user: String, key: String) {
        authenticationBridge[id] = Pair(user, key)
    }

    /**
     * Reads itself from the local file (`discord_auth.json`)
     */
    fun readSelf() {
        Accessor.readIfExists("discord_auth.json").then {
            val json = fromJson<HashMap<UUID, Long>>(it)!!
            authenticated.putAll(json)
        }.call()
    }

    /**
     * Stores itself in the local file (`discord_auth.json`)
     */
    fun storeSelf() {
        Accessor.overwrite("discord_auth.json", toJson(authenticated.toMap()))
        bot.shutdown()
    }

    /**
     * Performs initial setup for the discord bot
     */
    fun setupBot() {
        Threading.runAsync {
            var botBuilder =
                JDABuilder.create(discordBotToken, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(this)
            communicationChannel = Macrocosm.config.getLong("connections.discord.communication-channel")
            webhookLink = Macrocosm.config.getString("connections.discord.communication-webhook")
            if (communicationChannel != 0L && allOf(webhookLink != null, webhookLink != "NULL")) {
                Macrocosm.logger.info("Bot entering connection loop at $communicationChannel")
                botBuilder = botBuilder.addEventListeners(ConnectionLoop)
                ConnectionLoop.init()
            }

            bot = botBuilder.setActivity(Activity.playing("/help")).build()

            val commands = bot.updateCommands()

            commands.addCommands(
                Commands.slash("ping", "Gets the ping of Macrocosm Bot").setGuildOnly(true),
                Commands.slash("info", "Gets general info about Macrocosm")
                    .addOption(OptionType.STRING, "category", "Category for the bot to give info on", true, true),
                Commands.slash("auth", "Links your Minecraft account with this Discord account.")
                    .addOption(OptionType.STRING, "key", "Your unique authentication key", true, false),
                Commands.slash("bazaar", "Gets specific bazaar information")
                    .addOption(OptionType.STRING, "type", "Type of bazaar action", true, true)
                    .addOption(OptionType.STRING, "product", "Type of bazaar product, an identifier", false, true)
                    .addOption(OptionType.STRING, "user", "Username/UUID of user which bazaar to check", false, false),
                Commands.slash("profile", "Gets data on specific player")
                    .addOption(OptionType.USER, "user", "User which profile to check", false),
                Commands.slash("api", "Regenerates your Macrocosm API Key"),
                Commands.user("Macrocosm profile data")
            ).queue()


            taskRunLater(3 * 20L, sync = false) {
                val ch = communicationChannel
                if (ch != null) {
                    bot.updateCommands().addCommands(
                        Commands.slash("subscribe", "Subscribes you to the provided event announcements")
                            .addOption(OptionType.STRING, "event", "Event ID to subscribe you to", true, true),
                        Commands.slash("unsubscribe", "Unsubscribes you from the provided event announcements")
                            .addOption(OptionType.STRING, "event", "Event ID to unsubscribe you from", true, true)
                    ).queue()

                    val guild = bot.getGuildById(Macrocosm.config.getLong("connections.discord.guild-id"))!!
                    val c = guild.getTextChannelById(ch)!!
                    commTextChannel = c

                    // important roles
                    guild.getRolesByName("Macrocosm Boss Info", false).firstOrNull()?.apply {
                        Registry.DISCORD_EMITTERS.register(id("boss_info"), BossInfoEmitter(this, c))
                    } ?: run {
                        guild.createRole()
                            .setMentionable(true).setName("Macrocosm Boss Info").submit().thenAccept { role ->
                                Registry.DISCORD_EMITTERS.register(id("boss_info"), BossInfoEmitter(role, c))
                            }
                    }
                    guild.getRolesByName("Macrocosm Rare Drop", false).firstOrNull()?.apply {
                        Registry.DISCORD_EMITTERS.register(id("rare_drop"), RareDropEmitter(this, c))
                    } ?: run {
                        guild.createRole()
                            .setMentionable(true).setName("Macrocosm Rare Drop").submit().thenAccept { role ->
                                Registry.DISCORD_EMITTERS.register(id("rare_drop"), RareDropEmitter(role, c))
                            }
                    }
                    guild.getRolesByName("Macrocosm High Skill", false).firstOrNull()?.apply {
                        Registry.DISCORD_EMITTERS.register(id("high_skill"), HighSkillEmitter(this, c))
                    } ?: run {
                        guild.createRole()
                            .setMentionable(true).setName("Macrocosm High Skill").submit().thenAccept { role ->
                                Registry.DISCORD_EMITTERS.register(id("high_skill"), HighSkillEmitter(role, c))
                            }
                    }
                    guild.getRolesByName("Macrocosm Level Up", false).firstOrNull()?.apply {
                        Registry.DISCORD_EMITTERS.register(id("macrocosm_lvl_up"), MacrocosmLevelEmitter(this, c))
                    } ?: run {
                        guild.createRole()
                            .setMentionable(true).setName("Macrocosm Level Up").submit().thenAccept { role ->
                                Registry.DISCORD_EMITTERS.register(
                                    id("macrocosm_lvl_up"),
                                    MacrocosmLevelEmitter(role, c)
                                )
                            }
                    }
                }
            }
        }
    }

    override fun onUserContextInteraction(e: UserContextInteractionEvent) {
        when (e.name) {
            "Macrocosm profile data" -> {
                authOnly(e) { ctx, _ ->
                    profileCommand0(ctx, ctx.user, true)
                }
            }
        }
    }

    override fun onButtonInteraction(e: ButtonInteractionEvent) {
        when (e.componentId) {
            else -> {
                if (e.componentId.contains("next_order_buy")) {
                    val item = Identifier.parse(e.componentId.split("$").last())
                    val queue = bazaarBuyCache.getIfPresent(item) ?: Bazaar.table.topBuyOrders(item, 5)
                        .let { val q = it.toList().listIterator(); bazaarBuyCache.put(item, q); q }
                    val order =
                        (if (queue.hasNext()) queue.next() else return e.reply("Queue end reached!").setEphemeral(true)
                            .queue())
                    val embed = generateBuyOrderEmbed(item, order)
                    e.editMessage(MessageEditData.fromEmbeds(embed)).queue()
                } else if (e.componentId.contains("next_order_sell")) {
                    val item = Identifier.parse(e.componentId.split("$").last())
                    val queue = bazaarSellCache.getIfPresent(item) ?: Bazaar.table.topSellOrders(item, 5)
                        .let { val q = it.toList().listIterator(); bazaarSellCache.put(item, q); q }
                    val order =
                        (if (queue.hasNext()) queue.next() else return e.reply("Queue end reached!").setEphemeral(true)
                            .queue())
                    val embed = generateSellOrderEmbed(item, order)
                    e.editMessage(MessageEditData.fromEmbeds(embed)).queue()
                } else if (e.componentId.contains("prev_order_buy")) {
                    val item = Identifier.parse(e.componentId.split("$").last())
                    val queue = bazaarBuyCache.getIfPresent(item) ?: Bazaar.table.topBuyOrders(item, 5)
                        .let { val q = it.toList().listIterator(); bazaarBuyCache.put(item, q); q }
                    val order = (if (queue.hasPrevious()) queue.previous() else return e.reply("Queue end reached!")
                        .setEphemeral(true).queue())
                    val embed = generateBuyOrderEmbed(item, order)
                    e.editMessage(MessageEditData.fromEmbeds(embed)).queue()
                } else if (e.componentId.contains("prev_order_sell")) {
                    val item = Identifier.parse(e.componentId.split("$").last())
                    val queue = bazaarSellCache.getIfPresent(item) ?: Bazaar.table.topSellOrders(item, 5)
                        .let { val q = it.toList().listIterator(); bazaarSellCache.put(item, q); q }
                    val order = (if (queue.hasPrevious()) queue.previous() else return e.reply("Queue end reached!")
                        .setEphemeral(true).queue())
                    val embed = generateSellOrderEmbed(item, order)
                    e.editMessage(MessageEditData.fromEmbeds(embed)).queue()
                } else if (e.componentId.contains("prev_order_user")) {
                    val user = UUID.fromString(e.componentId.split("$").last())
                    val queue = bazaarUserCache.getIfPresent(user) ?: Bazaar.getOrdersForPlayer(user).listIterator()
                        .let { bazaarUserCache.put(user, it); it }
                    val order = (if (queue.hasPrevious()) queue.previous() else return e.reply("Queue end reached!")
                        .setEphemeral(true).queue())
                    val embed = when (order) {
                        is BazaarBuyOrder -> generateBuyOrderEmbed(
                            order.item,
                            order
                        )

                        is BazaarSellOrder -> generateSellOrderEmbed(order.item, order)
                        else -> unreachable()
                    }
                    e.editMessage(MessageEditData.fromEmbeds(embed)).queue()
                } else if (e.componentId.contains("next_order_user")) {
                    val user = UUID.fromString(e.componentId.split("$").last())
                    val queue = bazaarUserCache.getIfPresent(user) ?: Bazaar.getOrdersForPlayer(user).listIterator()
                        .let { bazaarUserCache.put(user, it); it }
                    val order =
                        (if (queue.hasNext()) queue.next() else return e.reply("Queue end reached!").setEphemeral(true)
                            .queue())
                    val embed = if (order is BazaarBuyOrder) generateBuyOrderEmbed(
                        order.item,
                        order
                    ) else if (order is BazaarSellOrder) generateSellOrderEmbed(order.item, order) else unreachable()
                    e.editMessage(MessageEditData.fromEmbeds(embed)).queue()
                }
            }
        }
    }

    private fun generateSellOrderEmbed(item: Identifier, order: BazaarSellOrder): MessageEmbed {
        val start = Instant.now()
        val mc = MacrocosmPlayer.loadPlayer(order.createdBy)
        val op = Bukkit.getOfflinePlayer(order.createdBy)
        return embed {
            setColor(COLOR_MACROCOSM)
            setTitle("**Bazaar Sell Order**")
            setFooter("Cached Bazaar Data")
            setAuthor(
                "Bazaar | Sell Order by ${mc?.rank?.format?.str()?.stripTags() ?: ""} ${op.name}",
                null,
                "https://crafatar.com/avatars/${order.createdBy}?overlay=true"
            )

            val ele = BazaarElement.idToElement(item)!!
            setThumbnail(itemImage(ele))

            addField("**Product:**", ele.name.str().stripTags(), true)
            addField("**Seller:**", "${mc?.rank?.format?.str()?.stripTags() ?: ""} ${op.name}", true)
            addField("**Created At:**", "<t:${Instant.ofEpochMilli(order.createdAt).epochSecond}>", true)
            addBlankField(false)
            addField("Amount Selling:", "${Formatting.withCommas(order.qty.toBigDecimal(), true)}x", true)
            addField("Price Per:", "**${Formatting.withCommas(order.pricePer.toBigDecimal())}** coins", true)
            addField("Original Amount:", "${Formatting.withCommas(order.originalAmount.toBigDecimal())}x", true)
            addBlankField(false)
            addField(
                "**Buyers:**", "```yml\n${
                    if (order.buyers.isEmpty()) "- None" else order.buyers.joinToString(separator = "\n") {
                        val mcSeller = MacrocosmPlayer.loadPlayer(order.createdBy)
                        val opSeller = Bukkit.getOfflinePlayer(order.createdBy)
                        "${mcSeller?.rank?.format?.str()?.stripTags() ?: ""} ${opSeller.name}"
                    }
                }\n```", false
            )

            addBlankField(false)

            val end = Duration.between(start, Instant.now()).toMillis()
            addField("**Time Took:**", "**${end}ms**", true)
        }
    }

    private fun generateBuyOrderEmbed(item: Identifier, order: BazaarBuyOrder): MessageEmbed {
        val start = Instant.now()
        val mc = MacrocosmPlayer.loadPlayer(order.createdBy)
        val op = Bukkit.getOfflinePlayer(order.createdBy)
        return embed {
            setColor(COLOR_MACROCOSM)
            setTitle("**Bazaar Buy Order**")
            setFooter("Cached Bazaar Data")
            setAuthor(
                "Bazaar | Buy Order by ${mc?.rank?.format?.str()?.stripTags() ?: ""} ${op.name}",
                null,
                "https://crafatar.com/avatars/${order.createdBy}?overlay=true"
            )

            val ele = BazaarElement.idToElement(item)!!
            setThumbnail(itemImage(ele))

            addField("**Product:**", ele.name.str().stripTags(), true)
            addField("**Buyer:**", "${mc?.rank?.format?.str()?.stripTags() ?: ""} ${op.name}", true)
            addField("**Created At:**", "<t:${Instant.ofEpochMilli(order.createdAt).epochSecond}>", true)
            addBlankField(false)
            addField("Amount Buying:", "${Formatting.withCommas(order.qty.toBigDecimal(), true)}x", true)
            addField("Price Per:", "**${Formatting.withCommas(order.pricePer.toBigDecimal())}** coins", true)
            addField("Original Amount:", "${Formatting.withCommas(order.originalAmount.toBigDecimal())}x", true)
            addBlankField(false)
            addField(
                "**Sellers:**", "```yml\n${
                    if (order.sellers.isEmpty()) "- None" else order.sellers.joinToString(separator = "\n") {
                        val mcSeller = MacrocosmPlayer.loadPlayer(order.createdBy)
                        val opSeller = Bukkit.getOfflinePlayer(order.createdBy)
                        "${mcSeller?.rank?.format?.str()?.stripTags() ?: ""} ${opSeller.name}"
                    }
                }\n```", false
            )

            addBlankField(false)

            val end = Duration.between(start, Instant.now()).toMillis()
            addField("**Time Took:**", "**${end}ms**", true)
        }
    }

    override fun onCommandAutoCompleteInteraction(e: CommandAutoCompleteInteractionEvent) {
        when (e.name) {
            "info" -> e.replyChoiceStrings(arrayOf("server", "bot").filter { it.startsWith(e.focusedOption.value) })
                .queue()

            "bazaar" -> when (e.focusedOption.name) {
                "type" -> e.replyChoiceStrings(
                    arrayOf(
                        "buy_orders",
                        "sell_orders",
                        "summary"
                    ).filter { it.startsWith(e.focusedOption.value) }).queue()

                "product" -> e.replyChoiceStrings(BazaarElement.allKeys.filter { it.path.contains(e.focusedOption.value) }
                    .map { it.toString() }.take(25)).queue()
            }

            "subscribe", "unsubscribe" -> e.replyChoiceStrings(Registry.DISCORD_EMITTERS.iter().keys.filter {
                it.path.contains(
                    e.focusedOption.value
                )
            }.take(25).map { it.toString() }).queue()
        }
    }

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        commandPool.execute {
            runCatchingReporting(e) {
                when (e.name) {
                    "ping" -> pingCommand(e)
                    "info" -> infoCommand(e)
                    "auth" -> authCommand(e)
                    "subscribe" -> subscribeCommand(e)
                    "unsubscribe" -> unsubscribeCommand(e)
                    "bazaar" -> authOnly(e, ::bazaarCommand)
                    "profile" -> authOnly(e, ::profileCommand)
                    "api" -> authOnly(e, ::apiCommand)
                    else -> {
                        logger.warning("Invalid command used: ${e.name}")
                        // invalid command
                    }
                }
            }
        }
    }

    override fun onSelectMenuInteraction(e: SelectMenuInteractionEvent) {
        if (e.componentId.contains("player_menu")) {
            val id = UUID.fromString(e.componentId.split("$").last())

            when (e.values[0]) {
                "general" -> {
                    e.editMessage(
                        MessageEditData.fromEmbeds(
                            profileGeneralEmbed(
                                MacrocosmPlayer.loadOrInit(id),
                                Bukkit.getOfflinePlayer(id)
                            )
                        )
                    ).queue()
                }

                "statistics" -> {
                    e.editMessage(
                        MessageEditData.fromEmbeds(
                            profileStatisticsEmbed(
                                MacrocosmPlayer.loadOrInit(id),
                                Bukkit.getOfflinePlayer(id)
                            )
                        )
                    ).queue()
                }

                "equipment" -> {
                    e.editMessage(
                        MessageEditData.fromEmbeds(
                            profileEquipmentEmbed(
                                MacrocosmPlayer.loadOrInit(id),
                                Bukkit.getOfflinePlayer(id)
                            )
                        )
                    ).queue()
                }

                "skills" -> {
                    e.editMessage(
                        MessageEditData.fromEmbeds(
                            profileSkillsEmbed(
                                MacrocosmPlayer.loadOrInit(id),
                                Bukkit.getOfflinePlayer(id)
                            )
                        )
                    ).queue()
                }
            }
        }
    }

    private fun unsubscribeCommand(e: GenericCommandInteractionEvent) {
        val member = e.member ?: return
        val emitterId = Identifier.parse(
            e.getOption("event")?.asString ?: return e.replyEmbeds(argMissingEmbed("event")).setEphemeral(true).queue()
        )
        val emitter = Registry.DISCORD_EMITTERS.findOrNull(emitterId) ?: return e.replyEmbeds(
            genericErrorEmbed(
                "Not Found",
                "Could not find event of id `$emitterId` to subscribe you!"
            ).build()
        ).setEphemeral(true).queue()
        emitter.unsubscribe(member)
        e.replyEmbeds(
            genericSuccessEmbed(
                "Unsubscription Successful",
                "Unsubscribed you from the *${emitter.name}* event announcements!"
            ).build()
        ).setEphemeral(true).queue()
    }

    private fun subscribeCommand(e: GenericCommandInteractionEvent) {
        val member = e.member ?: return
        val emitterId = Identifier.parse(
            e.getOption("event")?.asString ?: return e.replyEmbeds(argMissingEmbed("event")).setEphemeral(true).queue()
        )
        val emitter = Registry.DISCORD_EMITTERS.findOrNull(emitterId) ?: return e.replyEmbeds(
            genericErrorEmbed(
                "Not Found",
                "Could not find event of id `$emitterId` to subscribe you!"
            ).build()
        ).setEphemeral(true).queue()
        emitter.subscribe(member)
        e.replyEmbeds(
            genericSuccessEmbed(
                "Subscription Successful",
                "Subscribed you to the *${emitter.name}* event announcements!"
            ).build()
        ).setEphemeral(true).queue()
    }

    private fun apiCommand(e: GenericCommandInteractionEvent, op: OfflinePlayer) {
        val regeneratedKey = KeyManager.generateRandomKey(
            op.uniqueId,
            listOf(APIPermission.VIEW_BAZAAR_DATA, APIPermission.VIEW_PLAYER_DATA)
        )
        e.replyEmbeds(embed {
            setTitle("**API Key regeneration**")
            setColor(COLOR_MACROCOSM)

            addField("**New API Key**", "`$regeneratedKey`", false)
            addField(
                "**Usage**",
                "Use this key to access the Macrocosm API. More info on the /doc endpoint of API or in the swagger spec.",
                false
            )
        }).setEphemeral(true).queue()
    }

    private fun profileEquipmentEmbed(player: MacrocosmPlayer, op: OfflinePlayer): MessageEmbed {
        return embed {
            setTitle("**Player Equipment**")
            setAuthor(
                "${player.rank.format.str().stripTags()} ${op.name}'s Profile",
                null,
                "https://crafatar.com/avatars/${player.ref}?overlay=true"
            )
            setColor(COLOR_MACROCOSM)

            val equipment = player.equipment
            equipment.enumerate().withIndex().associateBy { PlayerEquipment.typesOrdered[it.index] }
                .forEach { (type, indexed) ->
                    val (_, stack) = indexed
                    if (stack == null) {
                        addField("**Item in ${type.name.replace("_", " ").capitalized()} Slot**", "`None`", false)
                    } else {
                        addField(
                            "**Item in ${type.name.replace("_", " ").capitalized()} Slot**",
                            "`${stack.buildName().str().stripTags()}`",
                            false
                        )
                    }
                }

            val activePet = player.activePet
            addField(
                "**Pet**",
                if (activePet == null) "`None`" else "`${
                    activePet.rarity(player).name.replace("_", " ").capitalized()
                } ${activePet.prototype.name.stripTags()} [Lv ${activePet.level(player)}]`",
                false
            )

            if (op.isOnline) {
                val online = op.player!!
                EquipmentSlot.values().forEach { slot ->
                    val item = online.inventory.getItem(slot)
                    val name = when (slot) {
                        EquipmentSlot.HAND -> "Main Hand"
                        EquipmentSlot.OFF_HAND -> "Off Hand"
                        EquipmentSlot.FEET -> "Boots"
                        EquipmentSlot.LEGS -> "Leggings"
                        EquipmentSlot.CHEST -> "Chestplate"
                        EquipmentSlot.HEAD -> "Helmet"
                    }
                    if (item.isAirOrNull()) {
                        addField("**Item in $name:**", "`None`", true)
                    } else {
                        addField(
                            "**Item in $name:**",
                            "`${item.macrocosm?.buildName()?.str()?.stripTags() ?: "None"}`",
                            true
                        )
                    }
                }
            } else {
                addField("**Player not online!**", "Can not get inventory data while player is not online!", false)
            }
        }
    }

    private fun profileSkillsEmbed(player: MacrocosmPlayer, op: OfflinePlayer): MessageEmbed {
        val skills = player.skills.skillExp

        return embed {
            setTitle("**Player Skills**")
            setAuthor(
                "${player.rank.format.str().stripTags()} ${op.name}'s Profile",
                null,
                "https://crafatar.com/avatars/${player.ref}?overlay=true"
            )
            setColor(COLOR_MACROCOSM)

            skills.entries.chunked(3).forEach { chunk ->
                chunk.forEach { (skill, value) ->
                    val totalExperience =
                        (if (value.lvl == 1) .0 else skill.inst.table.totalExpForLevel(value.lvl)) + value.overflow
                    addField(
                        "${skill.emoji} ${skill.inst.name}",
                        "${Formatting.withCommas(totalExperience.toBigDecimal(), false)} EXP (${value.lvl} LVL)",
                        true
                    )
                }
            }
        }
    }

    private fun profileStatisticsEmbed(player: MacrocosmPlayer, op: OfflinePlayer): MessageEmbed {
        val ogStats = player.stats()
        val originalStats = ogStats == null
        val stats = ogStats ?: player.baseStats
        return embed {
            setTitle("**Player Statistics**")
            setAuthor(
                "${player.rank.format.str().stripTags()} ${op.name}'s Profile",
                null,
                "https://crafatar.com/avatars/${player.ref}?overlay=true"
            )
            setColor(COLOR_MACROCOSM)

            addField(
                "**Raw Stats?**",
                if (originalStats) "These are stats **not** including equipment stats" else "These are stats **including** equipment stats",
                false
            )

            stats.iter().entries.chunked(3).forEach { chunk ->
                chunk.forEach { (stat, amount) ->
                    addField("**${stat.display.stripTags()}**", Formatting.withCommas(amount.toBigDecimal()), true)
                    stat.display
                }
            }
        }
    }

    private fun profileGeneralEmbed(player: MacrocosmPlayer, op: OfflinePlayer): MessageEmbed {
        return embed {
            setThumbnail("https://crafatar.com/renders/body/${player.ref}?overlay=true")
            setTitle("**General Player Information**")
            setAuthor(
                "${player.rank.format.str().stripTags()} ${op.name}'s Profile",
                null,
                "https://crafatar.com/avatars/${player.ref}?overlay=true"
            )
            setColor(COLOR_MACROCOSM)

            addField("**Username: **", op.name ?: "null", true)
            addField("**UUID: **", op.uniqueId.toString(), true)

            addBlankField(false)

            addField("**Rank: **", player.rank.name, true)
            addField("**Purse: **", "${Formatting.withFullCommas(player.purse)} coins", true)
            addField("**Bank: **", "${Formatting.withFullCommas(player.bank)} coins", true)

            addBlankField(false)

            addField("**Playtime: **", Duration.ofMillis(player.playtimeMillis()).toFancyString(), true)
            addField("**Joined: **", "<t:${Instant.ofEpochMilli(player.firstJoin).epochSecond}:R>", true)
            addField("**Last Seen: **", "<t:${Instant.ofEpochMilli(player.lastJoin).epochSecond}>", true)

            addBlankField(false)

            addField("**Online?**", op.isOnline.toString(), true)
            val authenticated = authenticated.containsKey(op.uniqueId)
            addField("**Have Authenticated?**", authenticated.toString(), true)
            if (authenticated) {
                val user = bot.retrieveUserById(this@Discord.authenticated[op.uniqueId]!!).submit().get()
                addField("**Discord Account:**", "`${user.asTag}`", true)
            }
        }
    }

    private fun profileCommand(e: GenericCommandInteractionEvent, op: OfflinePlayer) {
        profileCommand0(e, e.getOption("user")?.asUser ?: e.user, false)
    }

    private fun profileCommand0(e: GenericCommandInteractionEvent, user: User, ephemeral: Boolean) {
        val uuid = authenticated.entries.firstOrNull { (_, id) -> id == user.idLong }?.key ?: return e.replyEmbeds(
            genericErrorEmbed(
                "Not Found",
                "Could not find profile for user ${user.asTag}!\n*The `profile` command only works with authenticated users*"
            ).build()
        ).setEphemeral(true).queue()

        e.replyEmbeds(embed {
            setTitle("**Select Information Category**")
            setColor(COLOR_MACROCOSM)

            addField(
                "**Select Category**",
                "Select category of which you would like to get information on this player.",
                false
            )
        }).addActionRow(
            SelectMenu.create("player_menu$$uuid")
                .addOption("General Information", "general", "General information about this player")
                .addOption("Statistics", "statistics", "Gets in-game stats of this player")
                .addOption("Equipment", "equipment", "Gets some of player's in-game equipment")
                .addOption("Skills", "skills", "Gets in-game skills of this player")
                .build()
        ).setEphemeral(ephemeral).queue()

    }

    private fun bazaarCommand(e: GenericCommandInteractionEvent, op: OfflinePlayer) {
        val type =
            e.getOption("type")?.asString
        when (type) {
            "summary" -> {
                // require strictly item
                val msg = e.reply("Calculating bazaar summary, please wait...").submit().get()
                val start = Instant.now()

                val product = Identifier.parse(e.getOption("product")?.asString ?: run {
                    msg.editOriginal("`summary` action requires `product` argument!")
                    return
                })
                val summary = Bazaar.table.summary(product) ?: run {
                    msg.editOriginal(
                        MessageEditData.fromEmbeds(
                            genericErrorEmbed(
                                "Not Found",
                                "Could not find item of type `$product` in the bazaar!"
                            ).build()
                        )
                    ).queue()
                    return
                }
                val embed = embed {
                    setColor(COLOR_MACROCOSM)
                    setTitle("**Bazaar Item Summary**")
                    setFooter("Cached Bazaar Data")
                    setAuthor("Macrocosm Bazaar")

                    val ele = BazaarElement.idToElement(product)!!
                    setThumbnail(itemImage(ele))

                    addField("**Item:**", ele.name.str().stripTags(), true)
                    addField("**Total Orders:**", Formatting.withCommas(summary.ordersCount.toBigDecimal(), true), true)

                    val b = summary.buyOrders
                    val s = summary.sellOrders
                    addField("**Buy Orders:**", Formatting.withCommas(b.amount.toBigDecimal(), true), false)
                    addField("Average Buy Price: ", Formatting.withCommas(b.averagePrice.toBigDecimal()), true)
                    addField("Highest Buy Price: ", Formatting.withCommas(b.highestPrice.toBigDecimal()), true)
                    addField("Lowest Buy Price: ", Formatting.withCommas(b.lowestPrice.toBigDecimal()), true)
                    addField("Median Buy Price: ", Formatting.withCommas(b.medianPrice.toBigDecimal()), true)
                    addField("Cumulative Buy Coins: ", Formatting.withCommas(b.cumulativeCoins), true)
                    addField("Cumulative Buy Items: ", Formatting.withCommas(b.cumulativeItems.toBigDecimal()), true)

                    addBlankField(false)

                    addField("**Sell Orders:**", Formatting.withCommas(b.amount.toBigDecimal(), true), false)
                    addField("Average Sell Price: ", Formatting.withCommas(s.averagePrice.toBigDecimal()), true)
                    addField("Highest Sell Price: ", Formatting.withCommas(s.highestPrice.toBigDecimal()), true)
                    addField("Lowest Sell Price: ", Formatting.withCommas(s.lowestPrice.toBigDecimal()), true)
                    addField("Median Sell Price: ", Formatting.withCommas(s.medianPrice.toBigDecimal()), true)
                    addField("Cumulative Sell Coins: ", Formatting.withCommas(s.cumulativeCoins), true)
                    addField("Cumulative Sell Items: ", Formatting.withCommas(s.cumulativeItems.toBigDecimal()), true)

                    addBlankField(false)

                    val ms = Duration.between(start, Instant.now()).toMillis()
                    addField("**Time Took**", "**${ms}ms**", false)
                }

                msg.editOriginal(MessageEditBuilder().setContent("").setEmbeds(embed).build()).queue()
            }

            else -> {
                val username = e.getOption("user")?.asString
                val product = e.getOption("product")?.asString

                if (username == null && product != null) {
                    // querying product data
                    when (type) {
                        "buy_orders" -> {
                            val item = Identifier.parse(product)
                            val queue = bazaarBuyCache.getIfPresent(item) ?: Bazaar.table.topBuyOrders(item, 5)
                                .let { val q = it.toList().listIterator(); bazaarBuyCache.put(item, q); q }
                            val order = (if (queue.hasNext()) queue.next() else return e.replyEmbeds(
                                genericErrorEmbed(
                                    "Not Found",
                                    "Could not find any buy orders for this product!"
                                ).build()
                            ).setEphemeral(true).queue())
                            val embed = generateBuyOrderEmbed(item, order)
                            e.replyEmbeds(embed).addActionRow(
                                Button.primary("prev_order_buy$$item", "Previous Order"),
                                Button.primary("next_order_buy$$item", "Next Order")
                            ).queue()
                        }

                        "sell_orders" -> {
                            val item = Identifier.parse(product)
                            val queue = bazaarSellCache.getIfPresent(item) ?: Bazaar.table.topSellOrders(item, 5)
                                .let { val q = it.toList().listIterator(); bazaarSellCache.put(item, q); q }
                            val order = (if (queue.hasNext()) queue.next() else return e.replyEmbeds(
                                genericErrorEmbed(
                                    "Not Found",
                                    "Could not find any sell orders for this product!"
                                ).build()
                            ).setEphemeral(true).queue())
                            val embed = generateSellOrderEmbed(item, order)
                            e.replyEmbeds(embed).addActionRow(
                                Button.primary("prev_order_sell$$item", "Previous Order"),
                                Button.primary("next_order_sell$$item", "Next Order")
                            ).queue()
                        }
                    }
                } else if (product == null && username != null) {
                    // queueing data for a player
                    val uuid = try {
                        UUID.fromString(username)
                    } catch (e: Exception) {
                        Bukkit.getOfflinePlayer(username).uniqueId
                    }
                    val orders =
                        bazaarUserCache.getIfPresent(uuid) ?: Bazaar.getOrdersForPlayer(uuid).take(10).listIterator()
                            .let { bazaarUserCache.put(uuid, it); it }
                    val order = (if (orders.hasNext()) orders.next() else return e.replyEmbeds(
                        genericErrorEmbed(
                            "Not Found",
                            "Could not find any orders for this user!"
                        ).build()
                    ).setEphemeral(true).queue())
                    val embed = if (order is BazaarBuyOrder) generateBuyOrderEmbed(
                        order.item,
                        order
                    ) else if (order is BazaarSellOrder) generateSellOrderEmbed(order.item, order) else unreachable()
                    e.replyEmbeds(embed).addActionRow(
                        Button.primary("prev_order_user$$uuid", "Previous Order"),
                        Button.primary("next_order_user$$uuid", "Next Order")
                    ).queue()
                } else {
                    // invalid arguments provided
                    e.replyEmbeds(
                        genericErrorEmbed(
                            "Invalid Arguments",
                            "Invalid arguments provided for this command!"
                        ).build()
                    ).setEphemeral(true).queue()
                }
            }
        }
    }

    private fun authCommand(e: GenericCommandInteractionEvent) {
        val key = e.getOption("key")?.asString ?: return e.reply("Key not provided!").setEphemeral(true).queue()
        val fitting = authenticationBridge.entries.firstOrNull { (_, p) -> p.second == key }
        if (fitting == null) {
            return e.replyEmbeds(
                genericErrorEmbed(
                    "Not Found",
                    "You have not begun authentication process yet! Run `/discordauth <discord username>` to start!"
                ).build()
            ).setEphemeral(true).queue()
        } else if (authenticated.containsValue(e.idLong)) {
            return e.replyEmbeds(genericErrorEmbed("Already Authenticated", "You have already authenticated!").build())
                .setEphemeral(true).queue()
        } else {
            val (username, _) = fitting.value
            val currentUserName = "${e.user.name}#${e.user.discriminator}"
            if (username != currentUserName) {
                return e.replyEmbeds(
                    genericErrorEmbed(
                        "Invalid User",
                        "This authentication process was started by a different user!"
                    ).build()
                ).setEphemeral(true).queue()
            }
            authenticationBridge.remove(fitting.key)
            authenticated[fitting.key] = e.user.idLong
            e.replyEmbeds(
                genericSuccessEmbed(
                    "Authentication Successful!",
                    "Account `${Bukkit.getOfflinePlayer(fitting.key).name}` was linked with Discord account ${e.user.asMention}!"
                ).build()
            ).setEphemeral(true).queue()
        }
    }

    private fun infoCommand(e: GenericCommandInteractionEvent) {
        val category =
            e.getOption("category")?.asString ?: return e.reply("Category missing!").setEphemeral(true).queue()
        when (category) {
            "server" -> e.replyEmbeds(
                EmbedBuilder().addField("Test a", "A", true).setColor(NamedTextColor.LIGHT_PURPLE.value()).build()
            ).queue()

            "bot" -> e.replyEmbeds(
                EmbedBuilder().addField("Test b", "B", false).setColor(NamedTextColor.DARK_PURPLE.value()).build()
            ).queue()

            else -> e.reply("Invalid category!").setEphemeral(true).queue()
        }
    }

    private fun pingCommand(e: GenericCommandInteractionEvent) {
        val now = System.currentTimeMillis()
        e.reply("Pong!").setEphemeral(true)
            .flatMap { e.hook.editOriginal("Bot Ping: **${System.currentTimeMillis() - now}ms**") }.queue()
    }

    private fun genericErrorEmbed(error: String, message: String): EmbedBuilder {
        return EmbedBuilder().setTitle("**Error**").addField("**$error**", message, false).setColor(COLOR_RED)
    }

    private fun genericSuccessEmbed(title: String, message: String): EmbedBuilder {
        return EmbedBuilder().setTitle("**Success**").addField("**$title**", message, false).setColor(COLOR_MACROCOSM)
    }

    private fun argMissingEmbed(arg: String): MessageEmbed {
        return embed {
            setColor(COLOR_RED)
            setTitle("**Error**")
            addField("**Missing Argument**", "This command requires `$arg` argument to be present!", false)
        }
    }

    private inline fun <R> runCatchingReporting(ctx: SlashCommandInteractionEvent, executor: () -> R): Result<R> {
        val res = runCatching(executor)
        res.fold(
            onSuccess = { obj ->
                return Result.success(obj)
            },
            onFailure = { err ->
                val mc = err.macrocosm
                if (!ctx.isAcknowledged) {
                    // sending message to player directly
                    ctx.replyEmbeds(mc.embed).setEphemeral(true).addActionRow(
                        Button.link(mc.reportUrl, "Report This")
                    ).queue()
                } else {
                    ctx.guildChannel.sendMessageEmbeds(mc.embed).addContent(ctx.user.asMention).mention().addActionRow(
                        Button.link(mc.reportUrl, "Report This")
                    ).queue()
                }
                report("${mc.code}: ${mc.message}", nullFn())
                return Result.failure(mc)
            }
        )
    }

    private fun authOnly(
        e: GenericCommandInteractionEvent,
        command: (GenericCommandInteractionEvent, OfflinePlayer) -> Unit
    ) {
        val eId = e.user.idLong
        val uuid =
            authenticated.entries.firstOrNull { (_, id) -> id == eId }?.key ?: if (Macrocosm.isInDevEnvironment) {
                // we can ignore stuff in dev environment
                command(e, Bukkit.getOfflinePlayer("m_xus"))
                return
            } else
                return e.replyEmbeds(EMBED_AUTH_REQUIRED).setEphemeral(true).queue()
        command(e, Bukkit.getOfflinePlayer(uuid))
    }

    /**
     * Gets a valid URL that most well displays the provided item.
     *
     * It provides:
     * * => [https://mc-heads.net](https://mc-heads.net) if the item is a skull
     * * => [https://mcapi.marveldc.me](https://mcapi.marveldc.me) of the item is a block
     * * => [Macrocosm Data Location](https://github.com/Maxuss/Macrocosm-Data) otherwise
     *
     * Current limitations:
     * 1. Does not support enchant glint
     * 2. Does not support special macrocosm textures
     * 3. Does not support colored leather armor
     */
    fun itemImage(item: MacrocosmItem): String {
        val id = item.base.name.lowercase()
        if (id == "player_head") {
            val skin = when (item) {
                is SkullAbilityItem -> item.skin?.skin ?: item.skullOwner
                is RecipeItem -> item.skin?.skin ?: item.headSkin
                is PetItem -> {
                    val pet = item.stored ?: return "null"
                    if (pet.skin != null) {
                        (Registry.COSMETIC.find(pet.skin) as SkullSkin).skin
                    } else Registry.PET.find(pet.id).headSkin
                }

                is ReforgeStone -> item.skin?.skin ?: item.headSkin

                else -> unreachable() // we should not reach this
            }
            return try {
                val decoded = Base64.getDecoder().decode(skin).decodeToString()
                val jo = GSON.fromJson(decoded, JsonObject::class.java)
                val textureHash = jo["textures"].asJsonObject["SKIN"].asJsonObject["url"].asString.split("/").last()
                "https://mc-heads.net/head/$textureHash/150.png"
            } catch (e: IllegalArgumentException) {
                // it seems that a player's name was provided instead of a base64 object, use normal name instead
                "https://mc-heads.net/head/$skin/150.png"
            }
        }
        return if (item.base.isBlock) "https://mcapi.marveldc.me/item/$id?version=1.19&width=250&height=250&fuzzySearch=false" else "https://raw.githubusercontent.com/Maxuss/Macrocosm-Data/master/items/generated/${id}.png"
    }

    /**
     * Sends embed that displays the difference between current version and previous version
     */
    fun sendVersionDiff(previous: SemanticVersion) {
        commTextChannel?.sendMessage(MessageCreateData.fromEmbeds(embed {
            setColor(0x29232D)
            setTitle("**Macrocosm Version Change**")
            addField("", "Version $previous → ${Macrocosm.version} \uD83E\uDE79", true)
            addBlankField(false)
            addField(
                "",
                (if (previous.major == Macrocosm.version.major) "The **Macrocosm version** has changed! *Something* was patched, fixed, updated, or added!\nKeep guessing what it could be!" else "The **Macrocosm version** has undergone major change! This is a **big** update!"),
                true
            )

            setTimestamp(Instant.now())
        }))!!.queue()
    }

    /**
     * Sends all the new items introduced since the last restart. Uses [StackRenderer] under the hood.
     */
    fun sendItemDiffs(it: Identifier) {
        val mc = Registry.ITEM.find(it)
        val item = mc.build(null) ?: return
        // sending image to buffer channel
        StackRenderer(ItemRenderBuffer.stack(item)) { InternalMacrocosmPlugin.FONT_MINECRAFT.deriveFont(50f) }.renderToFile(
            "item_renders/${it.path}.png"
        ).thenAccept { _ ->
            commTextChannel?.sendMessage(MessageCreateBuilder().setEmbeds(embed {
                setColor(COLOR_MACROCOSM)
                setTitle("**New Items!**")
                addField(
                    "**${mc.buildName().str().stripTags()}**",
                    "New item of **${
                        mc.rarity.name.replace(
                            "_",
                            " "
                        )
                    }** commodity!\nItem ID: `$it`\nMore info in the API (or in game)!",
                    false
                )
                addField(
                    "**API Endpoint URL**",
                    "`https://${if (Macrocosm.isInDevEnvironment) "127.0.0.1" else currentIp}/v2/resources/item/${it}`",
                    false
                )
                val thumbnailUrl = itemImage(mc)
                setThumbnail(thumbnailUrl)
                setImage("attachment://${it.path}.png")
            }).setFiles(FileUpload.fromData(Accessor.access("item_renders/${it.path}.png"))).build())!!.submit()
                .thenAccept { _ ->
                    // meanwhile delete the original render
                    Threading.runAsync(isDaemon = true) {
                        Accessor.access("item_renders/${it.path}.png").deleteIfExists()
                    }
                }
        }
    }

    /**
     * Finds a player's avatar via the [Crafatar](https://crafatar.com)
     */
    fun playerAvatar(player: MacrocosmPlayer): String {
        return "https://crafatar.com/avatars/${player.ref}?overlay=true"
    }

    /**
     * Gets an authenticated discord user or null
     */
    fun getAuthenticatedOrNull(player: MacrocosmPlayer): User? {
        return bot.retrieveUserById(authenticated[player.ref] ?: return null).submit().get()
    }

    /**
     * Constructs a new embed from a [builder]
     *
     * @see EmbedBuilder
     */
    inline fun embed(builder: EmbedBuilder.() -> Unit): MessageEmbed {
        return EmbedBuilder().setFooter(
            "Macrocosm",
            "https://cdn.discordapp.com/attachments/846281911332896818/1014934635618258984/pack.png"
        ).setTimestamp(
            Instant.now()
        ).apply(builder).build()
    }
}
