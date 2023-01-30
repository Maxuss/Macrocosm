package space.maxus.macrocosm.zone

import org.bukkit.Location
import space.maxus.macrocosm.registry.Identifier
import java.awt.Polygon


class PolygonalZone(name: String, id: String, val vertices: List<Location>): Zone(Identifier.parse(id), name) {
    private val polygon: Polygon
    private val lowestPoint: Double
    private val highestPoint: Double

    init {
        val xPoints: MutableList<Int> = ArrayList()
        val yPoints: MutableList<Int> = ArrayList()
        vertices.forEach { point ->
            xPoints.add(point.blockX)
            yPoints.add(point.blockZ)
        }
        polygon = Polygon(xPoints.toIntArray(), yPoints.toIntArray(), xPoints.size)
        lowestPoint = vertices.minOf { it.y }
        highestPoint = vertices.maxOf { it.y }
    }

    override fun contains(location: Location): Boolean {
        return polygon.contains(location.x, location.z) && location.y in (lowestPoint..highestPoint)
    }
}
