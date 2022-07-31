package space.maxus.macrocosm.util.math

import org.bukkit.Location
import org.bukkit.World

data class Point(val x: Float, val y: Float, val z: Float) {
    companion object {
        fun from(loc: Location): Point {
            return Point(loc.x.toFloat(), loc.y.toFloat(), loc.z.toFloat())
        }
    }


    fun to(world: World): Location {
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }
}
