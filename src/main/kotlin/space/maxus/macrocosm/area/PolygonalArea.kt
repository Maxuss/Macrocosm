package space.maxus.macrocosm.area

import org.bukkit.Location
import space.maxus.macrocosm.registry.Identifier
import java.awt.Polygon


/**
 * A polygonal (not polyhedral) area
 */
class PolygonalArea(
    /**
     * ID of this area
     */
    id: String,
    /**
     * All points of this polygon
     */
    val vertices: List<Location>
): Area(Identifier.parse(id)) {
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
