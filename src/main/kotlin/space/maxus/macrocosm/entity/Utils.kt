package space.maxus.macrocosm.entity

import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import space.maxus.macrocosm.text.text

fun raycast(from: LivingEntity, distance: Int): Location {
    return try {
        val eyes: Location = from.eyeLocation
        val iterator = BlockIterator(from.location, 1.0, distance)
        while (iterator.hasNext()) {
            val loc: Location = iterator.next().location
            if (loc.block.type.isSolid) {
                if (loc == from.location) {
                    from.sendMessage(text("<red>There are blocks in the way!"))
                }
                loc.pitch = eyes.pitch
                loc.yaw = eyes.yaw
                loc.y += 1
                return loc
            }
        }
        val n: Location = from.eyeLocation.clone().add(from.eyeLocation.direction.multiply(distance))
        n.pitch = eyes.pitch
        n.yaw = eyes.yaw
        n.y = n.y + 1
        n
    } catch (e: IllegalStateException) {
        from.sendMessage(text("<red>There are blocks in the way!"))
        from.location
    }
}

fun raycast(from: Location, direction: Vector, distance: Int): Location {
    from.direction = direction
    val iterator = BlockIterator(from, 1.0, distance)
    while (iterator.hasNext()) {
        val loc: Location = iterator.next().location
        if (loc.block.type.isSolid) {
            loc.y += 1
            return loc
        }
    }
    val n: Location = from.clone().add(from.direction.multiply(distance))
    n.y = n.y + 1
    return n
}
