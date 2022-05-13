package space.maxus.macrocosm.util

import com.google.gson.GsonBuilder
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player

val GSON = GsonBuilder().create()

fun Player.sendPacket(packet: Packet<PacketListener>) {
    (this as CraftPlayer).handle.networkManager.send(packet)
}
