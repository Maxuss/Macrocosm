package space.maxus.macrocosm.net

import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.handler.codec.MessageToByteEncoder
import io.papermc.paper.network.ChannelInitializeListener
import io.papermc.paper.network.ChannelInitializeListenerHolder
import net.kyori.adventure.key.Key
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.ClientboundPlayerChatHeaderPacket
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import space.maxus.macrocosm.util.GSON
import java.lang.reflect.Proxy


@ChannelHandler.Sharable
object SignatureDissolver: MessageToByteEncoder<ClientboundStatusResponsePacket>(), Listener {
    fun inject() {
        val initListener =
            Proxy.newProxyInstance(this.javaClass.classLoader, arrayOf<Class<*>>(ChannelInitializeListener::class.java)) { proxy, method, args ->
                if (method.name == "afterInitChannel" && method.parameterCount == 1 && method.parameterTypes[0] === Channel::class.java
                ) {
                    val channel: Channel = args[0] as Channel
                    channel.pipeline().addAfter("packet_handler", "macrocosm_status_handler", this)
                    return@newProxyInstance null
                }
                method.invoke(proxy, args)
            }
        ChannelInitializeListenerHolder.addListener(Key.key("macrocosm", "signature_dissolver"), initListener as ChannelInitializeListener)
    }

    override fun encode(ctx: ChannelHandlerContext, msg: ClientboundStatusResponsePacket, out: ByteBuf) {
        val tree = GSON.toJsonTree(msg.status)
        tree.asJsonObject.addProperty("preventsChatReports", true)
        val buf = FriendlyByteBuf(out)
        buf.writeVarInt(ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getPacketId(PacketFlow.CLIENTBOUND, msg) ?: return)
        buf.writeUtf(GSON.toJson(tree))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onJoin(e: PlayerJoinEvent) {
        val nmsPlayer = (e.player as CraftPlayer).handle
        val pipeline = nmsPlayer.connection.connection.channel.pipeline()

        pipeline.addAfter("packet_handler", "signature_dissolver", object: ChannelDuplexHandler() {
            override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                if(msg is ClientboundPlayerChatPacket) {
                    val content =
                        msg.message().unsignedContent().orElse(msg.message().signedContent().decorated())

                    val ctbo = msg.chatType().resolve(nmsPlayer.level.registryAccess())
                    if (ctbo.isEmpty) {
                        return
                    }
                    val decoratedContent = ctbo.orElseThrow().decorate(content)

                    super.write(ctx, ClientboundSystemChatPacket(decoratedContent, false), promise)
                    return
                }
                if(msg is ClientboundPlayerChatHeaderPacket) {
                    return
                }

                super.write(ctx, msg, promise)
            }
        })
    }
}
