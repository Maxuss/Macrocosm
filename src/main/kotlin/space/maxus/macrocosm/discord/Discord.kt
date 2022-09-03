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
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.GatewayIntent
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
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.bazaar.*
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.db.Accessor
import space.maxus.macrocosm.discordBotToken
import space.maxus.macrocosm.exceptions.macrocosm
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.RecipeItem
import space.maxus.macrocosm.item.SkullAbilityItem
import space.maxus.macrocosm.logger
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.*
import space.maxus.macrocosm.util.general.ConditionalValueCallback
import space.maxus.macrocosm.util.metrics.report
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.ExecutorService

@Suppress("UNUSED_PARAMETER")
object Discord : ListenerAdapter() {
    object ConnectionLoop : ListenerAdapter(), Listener {
        private val connectionPool: ExecutorService = Threading.newFixedPool(16)
        private var client: JDAWebhookClient? = null

        fun init() {
            client = WebhookClientBuilder(webhookLink!!).setThreadFactory(
                ThreadFactoryBuilder().setUncaughtExceptionHandler(Monitor.exceptionHandler)
                    .setNameFormat("Webhook Thread").setDaemon(true).build()
            ).setWait(true).buildJDA()
            Macrocosm.logger.info("Webhook connection initialized!")
        }

        fun trySendMessage(msgStr: String, player: Player) {
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
                if (event.message.author.idLong == 1014930457353265255) {
                    // ignore our own fake-user webhook messages
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
    lateinit var bot: JDA
    private var communicationChannel: Long? = null
    private var webhookLink: String? = null
    private val bazaarSellCache: Cache<Identifier, ListIterator<BazaarSellOrder>> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()
    private val bazaarBuyCache: Cache<Identifier, ListIterator<BazaarBuyOrder>> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()
    private val bazaarUserCache: Cache<UUID, ListIterator<BazaarOrder>> =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build()

    const val COLOR_RED = 0xBF0000
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

    fun hasBegunAuth(id: UUID): ConditionalValueCallback<String> {
        return if (authenticationBridge.containsKey(id)) {
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
            val json = fromJson<HashMap<UUID, Long>>(it)!!
            authenticated.putAll(json)
        }.call()
    }

    fun storeSelf() {
        Accessor.overwrite("discord_auth.json", toJson(authenticated.toMap()))
        bot.shutdown()
    }

    fun setupBot() {
        Threading.runAsyncRaw {
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
                    .addOption(OptionType.STRING, "user", "Username of user which bazaar to check", false, false)
            ).queue()

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
                    val embed = if (order is BazaarBuyOrder) generateBuyOrderEmbed(
                        order.item,
                        order
                    ) else if (order is BazaarSellOrder) generateSellOrderEmbed(order.item, order) else unreachable()
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
            setThumbnail(bazaarItemThumbnail(ele))

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
            setThumbnail(bazaarItemThumbnail(ele))

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
        }
    }

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        commandPool.execute {
            runCatchingReporting(e) {
                when (e.name) {
                    "ping" -> pingCommand(e)
                    "info" -> infoCommand(e)
                    "auth" -> authCommand(e)
                    "bazaar" -> authOnly(e, ::bazaarCommand)
                    else -> {
                        logger.warning("Invalid command used: ${e.name}")
                        // invalid command
                    }
                }
            }
        }
    }

    private fun bazaarCommand(e: SlashCommandInteractionEvent, op: OfflinePlayer) {
        val type =
            e.getOption("type")?.asString ?: return e.reply("Action Type not provided!").setEphemeral(true).queue()
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
                    setThumbnail(bazaarItemThumbnail(ele))

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

    private fun authCommand(e: SlashCommandInteractionEvent) {
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

    private fun infoCommand(e: SlashCommandInteractionEvent) {
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

    private fun pingCommand(e: SlashCommandInteractionEvent) {
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
        e: SlashCommandInteractionEvent,
        command: (SlashCommandInteractionEvent, OfflinePlayer) -> Unit
    ) {
        val eId = e.user.idLong
        val uuid =
            authenticated.entries.firstOrNull { (_, id) -> id == eId }?.key ?: return e.replyEmbeds(EMBED_AUTH_REQUIRED)
                .setEphemeral(true).queue()
        command(e, Bukkit.getOfflinePlayer(uuid))
    }

    private fun bazaarItemThumbnail(item: MacrocosmItem): String {
        val id = item.base.name.lowercase()
        if (id == "player_head") {
            val skin = when (item) {
                is SkullAbilityItem -> item.skullOwner
                is RecipeItem -> item.headSkin
                else -> unreachable() // we should not reach this
            }
            return try {
                val jo = GSON.fromJson(Base64.getDecoder().decode(skin).decodeToString(), JsonObject::class.java)
                val textureHash = jo["textures"].asJsonObject["SKIN"].asJsonObject["url"].asString.split("/").last()
                "https://mc-heads.net/head/$textureHash/150.png"
            } catch (e: IllegalArgumentException) {
                // it seems that a player's name was provided instead of a base64 object, use normal name instead
                "https://mc-heads.net/head/$skin/150.png"
            }
        }
        return if (item.base.isBlock) "https://mcapi.marveldc.me/item/$id?version=1.19&width=250&height=250&fuzzySearch=false" else "https://raw.githubusercontent.com/Maxuss/Macrocosm-Data/master/items/generated/${id}.png"
    }

    inline fun embed(builder: EmbedBuilder.() -> Unit): MessageEmbed {
        return EmbedBuilder().setFooter(
            "Macrocosm",
            "https://cdn.discordapp.com/attachments/846281911332896818/1014934635618258984/pack.png"
        ).setTimestamp(
            Instant.now()
        ).apply(builder).build()
    }
}
