import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.lang.UnsupportedOperationException
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Bounds(val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)
data class Coord(val x: Int, val y: Int)
val Coord.manhattan get() = abs(x) + abs(y)
data class LineSegment(val start: Coord, val end: Coord)
data class Line(val id: Int, val segments: List<LineSegment>)
data class Intersection(val coord: Coord, val line: Line, val length: Int)

fun List<LineSegment>.bounds(): Bounds {
    var xMin: Int = Int.MAX_VALUE
    var yMin: Int = Int.MAX_VALUE
    var xMax: Int = Int.MIN_VALUE
    var yMax: Int = Int.MIN_VALUE

    forEach {
        xMin = min(xMin, min(it.start.x, it.end.x))
        yMin = min(yMin, min(it.start.y, it.end.y))
        xMax = max(xMax, max(it.start.x, it.end.x))
        yMax = max(yMax, max(it.start.y, it.end.y))
    }

    return Bounds(xMin, yMin, xMax, yMax)
}

inline fun Line.forEachCoordinate(body: (x: Int, y: Int, l: Int) -> Unit) {
    var l = 0
    segments.forEachIndexed { idx, it ->
        l = it.forEachCoordinate(l, if (idx == 0 || idx == (segments.size - 1)) 0 else 1, body)
    }
}

inline fun LineSegment.forEachCoordinate(initialLength: Int = 0, skip: Int = 0, body: (x: Int, y: Int, l: Int) -> Unit): Int {
    var length = initialLength
    var skipped = -skip
    val rangeX = IntProgression.fromClosedRange(start.x, end.x, if (end.x > start.x) 1 else -1)
    val rangeY = IntProgression.fromClosedRange(start.y, end.y, if (end.y > start.y) 1 else -1)
    rangeX.forEach { x ->
        rangeY.forEach { y ->
            if (skipped++ >=0)
                body(x, y, length++)
        }
    }
    return length
}

class Grid(val bounds: Bounds) {
    val width = bounds.xMax - bounds.xMin + 1
    val height = bounds.yMax - bounds.yMin + 1

    private val backing = IntArray(width * height) { 0 }

    fun get(x: Int, y: Int): Int {
        return getRaw(x - bounds.xMin, y - bounds.yMin)
    }

    fun getRaw(x: Int, y: Int): Int {
        return backing[(y)*width+(x)]
    }

    fun set(x: Int, y: Int, value: Int) {
        backing[(y-bounds.yMin)*width+(x-bounds.xMin)] = value
    }
}

fun main() {
    val input by lazy {
        File("Day03.txt").readLines(Charsets.UTF_8)
    }

    val lines = input.mapIndexed { id, it ->
        val segments = it.split(",")
        var currentX = 0;
        var currentY = 0;
        Line(id, segments.map {
            val length = it.substring(1).toInt()
            when (it[0]) {
                'U' -> LineSegment(Coord(currentX, currentY), Coord(currentX, currentY - length))
                    .also { currentY -= length }
                'D' -> LineSegment(Coord(currentX, currentY), Coord(currentX, currentY + length))
                    .also { currentY += length }
                'L' -> LineSegment(Coord(currentX, currentY), Coord(currentX - length, currentY))
                    .also { currentX -= length }
                'R' -> LineSegment(Coord(currentX, currentY), Coord(currentX + length, currentY))
                    .also { currentX += length }
                else -> throw UnsupportedOperationException()
            }
        })
    }

    val bounds = lines.flatMap { it.segments }.bounds()
    val grid = Grid(bounds)

    val intersectionMap = mutableMapOf<Coord, MutableList<Intersection>>()
    fun draw(grid: Grid, line: Line): List<Intersection> {
        val lineValue = (1 shl line.id)
        val intersections = mutableListOf<Intersection>()
        line.forEachCoordinate { x, y, l ->
            val currentValue = grid.get(x, y)
            grid.set(x, y,  lineValue)
            if (currentValue or lineValue != lineValue) {
                intersections += Intersection(Coord(x, y), line, l)
            }
        }
        return intersections
    }

    sequenceOf(lines[0], lines[1], lines[0]).forEach { line ->
        val intersections = draw(grid, line)
        intersections.forEach { intersection ->
            intersectionMap.putIfAbsent(intersection.coord, mutableListOf())
            intersectionMap[intersection.coord]!!.add(intersection)
        }
    }

    val closestIntersection = intersectionMap.keys
        .filter { it.manhattan != 0 }
        .minBy { it.manhattan }!!
    println("Assignment A: closest intersection @ ${closestIntersection}, distance: ${closestIntersection.manhattan}")

    val shortestPath = intersectionMap
        .filterKeys { it.manhattan != 0 }
        .minBy { (_, v) -> v[0].length + v[1].length }!!
    println("Assignment B: shortest intersection @ ${shortestPath.key}, length: ${shortestPath.value[0].length + shortestPath.value[1].length}")

    val image = BufferedImage(grid.width, grid.height, TYPE_INT_RGB)
    (0 until grid.width).forEach { x ->
        (0 until grid.height).forEach { y ->
            when (grid.getRaw(x, y)) {
                0 -> image.setRGB(x, y, Color.WHITE.rgb)
                1 -> image.setRGB(x, y, Color.BLACK.rgb)
                2 -> image.setRGB(x, y, Color.RED.rgb)
                else -> image.setRGB(x, y, Color.GREEN.rgb)
            }
        }
    }
    ImageIO.write(image, "png", File("debug.png"))
}
