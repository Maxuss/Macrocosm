package space.maxus.macrocosm.zone

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import space.maxus.macrocosm.data.level.LevelDbAdapter
import space.maxus.macrocosm.events.PlayerEnterZoneEvent
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry

object ZoneLevelDbAdapter: LevelDbAdapter("Zone"), Listener {
    override fun save(to: CompoundTag) {
        for((id, zone) in Registry.ZONE.iter()) {
            val cmp = CompoundTag()
            cmp.putString("Name", zone.name)
            if(zone is PolygonalZone) {
                val list = ListTag()
                for(vertex in zone.vertices) {
                    val vertexCompound = CompoundTag()
                    vertexCompound.putInt("X", vertex.blockX)
                    vertexCompound.putInt("Y", vertex.blockY)
                    vertexCompound.putInt("Z", vertex.blockZ)
                    list.add(vertexCompound)
                }
                cmp.put("Vertices", list)
            } else if(zone is RestrictiveZone) {
                val list = ListTag()
                for(vertex in zone.inner.vertices) {
                    val vertexCompound = CompoundTag()
                    vertexCompound.putInt("X", vertex.blockX)
                    vertexCompound.putInt("Y", vertex.blockY)
                    vertexCompound.putInt("Z", vertex.blockZ)
                    list.add(vertexCompound)
                }
                cmp.put("Vertices", list)
                val restrictionCompound = CompoundTag()
                restrictionCompound.putInt("X", zone.exit.blockX)
                restrictionCompound.putInt("Y", zone.exit.blockY)
                restrictionCompound.putInt("Z", zone.exit.blockZ)
                cmp.put("RestrictionExit", restrictionCompound)
            } else continue
            to.put(id.toString(), cmp)
        }
    }

    override fun load(from: CompoundTag) {
        val world = Bukkit.getWorlds().first()
        for(key in from.allKeys) {
            val id = Identifier.parse(key)
            val cmp = from.getCompound(key)
            val name = cmp.getString("Name")
            val vertices = cmp.getList("Vertices", CompoundTag.TAG_COMPOUND.toInt()).map {
                val each = it as CompoundTag
                val x = each.getInt("X")
                val y = each.getInt("Y")
                val z = each.getInt("Z")
                Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            }
            val final: Zone = if(cmp.contains("RestrictionExit")) {
                val each = cmp.getCompound("RestrictionExit")
                val x = each.getInt("X")
                val y = each.getInt("Y")
                val z = each.getInt("Z")
                val exit = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                RestrictiveZone(PolygonalZone(name, key, vertices), exit)
            } else {
                PolygonalZone(name, key, vertices)
            }
            Registry.ZONE.register(id, final)
        }
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if(!e.hasChangedBlock())
            return

        e.player.macrocosm?.calculateZone()
    }

    @EventHandler
    fun onEnterZone(e: PlayerEnterZoneEvent) {
        if(e.newZone is RestrictiveZone)
            e.isCancelled = true
    }
}

