package space.maxus.macrocosm.area

import com.google.common.collect.HashMultimap
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerMoveEvent
import space.maxus.macrocosm.area.spawning.SpawningPosition
import space.maxus.macrocosm.data.level.LevelDbAdapter
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.util.*

/**
 * A LevelDB adapter for Areas
 */
object AreaLevelDbAdapter: LevelDbAdapter("Area"), Listener {
    val spawnsPerArea: HashMultimap<Identifier, UUID> = HashMultimap.create()

    override fun save(to: CompoundTag) {
        for((id, zone) in Registry.AREA.iter()) {
            val cmp = CompoundTag()
            if(zone is PolygonalArea) {
                val list = ListTag()
                for(vertex in zone.vertices) {
                    val vertexCompound = CompoundTag()
                    vertexCompound.putInt("X", vertex.blockX)
                    vertexCompound.putInt("Y", vertex.blockY)
                    vertexCompound.putInt("Z", vertex.blockZ)
                    list.add(vertexCompound)
                }
                cmp.put("Vertices", list)
            } else if(zone is RestrictedArea) {
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
            val spawns = ListTag()
            for(spawn in zone.spawns) {
                spawns.add(spawn.save())
            }
            cmp.put("Spawns", spawns)
            to.put(id.toString(), cmp)
        }
    }

    override fun load(from: CompoundTag) {
        val world = Bukkit.getWorlds().first()
        for(key in from.allKeys) {
            val id = Identifier.parse(key)
            val cmp = from.getCompound(key)
            val vertices = cmp.getList("Vertices", CompoundTag.TAG_COMPOUND.toInt()).map {
                val each = it as CompoundTag
                val x = each.getInt("X")
                val y = each.getInt("Y")
                val z = each.getInt("Z")
                Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            }
            val final: Area = if(cmp.contains("RestrictionExit")) {
                val each = cmp.getCompound("RestrictionExit")
                val x = each.getInt("X")
                val y = each.getInt("Y")
                val z = each.getInt("Z")
                val exit = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
                RestrictedArea(PolygonalArea(key, vertices), exit)
            } else {
                PolygonalArea(key, vertices)
            }
            for(spawn in cmp.getList("Spawns", Tag.TAG_COMPOUND.toInt())) {
                val cm = spawn as CompoundTag
                final.spawns.add(SpawningPosition.read(cm))
            }
            Registry.AREA.register(id, final)
        }
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        val entry = spawnsPerArea.entries().firstOrNull { it.value == e.entity.uniqueId } ?: return
        val allSpawned = spawnsPerArea.get(entry.key)
        allSpawned.remove(entry.value)
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if(!e.hasChangedBlock())
            return

        e.player.macrocosm?.calculateZone()
    }
}

