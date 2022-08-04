package space.maxus.macrocosm.util.math

import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.atan

object MathHelper {
    fun parabola(loc1: Location, loc2: Location, points: Int): List<Location> {
        val locList = ArrayList<Location>()
        val distance = loc1.distance(loc2)
        for (i in 1 until points) {
            val ratio = i.toDouble() / points
            val location = loc1.lerp(loc2, ratio)
            val distanceToMid = abs(ratio - 0.5)
            val additionalHeight = (-(4.0 * (distance / 3.0)) * Mth.square(distanceToMid)) + (distance / 3.0)
            location.add(0.0, additionalHeight, 0.0)
            locList.add(location)
        }
        return locList
    }

    fun lerp(point1: Double, point2: Double, alpha: Double): Double {
        return point1 + alpha * (point2 - point1)
    }

    fun Location.lerp(loc2: Location, alpha: Double): Location {
        val xLerp = lerp(x, loc2.x, alpha)
        val yLerp = lerp(y, loc2.y, alpha)
        val zLerp = lerp(z, loc2.z, alpha)
        return Location(world, xLerp, yLerp, zLerp)
    }

    fun Vector.extractYawPitch(): Pair<Float, Float> {
        val pitch: Float
        if (x == 0.0 && z == 0.0) {
            pitch = if (y > 0) -90f else 90f
            return Pair(0f, pitch)
        }

        val theta = Mth.atan2(-x, z)
        val yaw = Math.toDegrees((theta + Mth.TWO_PI) % Mth.TWO_PI).toFloat()

        val x2 = x * x
        val z2 = z * z
        val xz = Mth.sqrt((x2 + z2).toFloat())
        pitch = Math.toDegrees(atan(-y / xz)).toFloat()

        return Pair(yaw, pitch)
    }
}
