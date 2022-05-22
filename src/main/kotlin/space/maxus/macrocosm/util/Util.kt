package space.maxus.macrocosm.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import net.minecraft.network.PacketListener
import net.minecraft.network.protocol.Packet
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import space.maxus.macrocosm.chat.ComponentTypeAdapter
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.IdentifierTypeAdapter
import space.maxus.macrocosm.stats.SpecialStatisticTypeAdapter
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.StatisticTypeAdapter
import space.maxus.macrocosm.stats.Statistics

val GSON: Gson = GsonBuilder()
    .registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
    .registerTypeAdapter(Statistics::class.java, StatisticTypeAdapter)
    .registerTypeAdapter(SpecialStatistics::class.java, SpecialStatisticTypeAdapter)
    .registerTypeAdapter(Component::class.java, ComponentTypeAdapter)
    .create()
val GSON_PRETTY: Gson = GsonBuilder()
    .disableHtmlEscaping()
    .registerTypeAdapter(Identifier::class.java, IdentifierTypeAdapter)
    .registerTypeAdapter(Statistics::class.java, StatisticTypeAdapter)
    .registerTypeAdapter(SpecialStatistics::class.java, SpecialStatisticTypeAdapter)
    .registerTypeAdapter(Component::class.java, ComponentTypeAdapter)
    .setPrettyPrinting().create()
fun <L : PacketListener, P : Packet<L>> Player.sendPacket(packet: P) {
    (this as CraftPlayer).handle.networkManager.send(packet)
}

fun EntityType.summon(location: Location): Entity {
    val types = net.minecraft.world.entity.EntityType.byString(this.key.asString())
    val nmsEntity = (if (types.isPresent) {
        val entityTypes = types.get()
        entityTypes.create((location.world as CraftWorld).handle)
    } else null) ?: throw RuntimeException("Can not spawn an entity of type $this!")
    nmsEntity.bukkitEntity.teleport(location)
    return nmsEntity.bukkitEntity
}
