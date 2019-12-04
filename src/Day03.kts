
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Bounds(val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)
data class Coord(val x: Int, val y: Int)
data class LineSegment(val start: Coord, val end: Coord, val direction: Direction, val length: Int)
data class Line(val id: Int, val segments: List<LineSegment>)
data class Intersection(val coord: Coord, val lengthA: Int, val lengthB: Int)

fun Coord.manhattan() = abs(x) + abs(y)

fun Line.bounds() = segments.map { it.bounds() }.flatten()

fun LineSegment.bounds() = Bounds(
    xMin = min(start.x, end.x),
    yMin = min(start.y, end.y),
    xMax = max(start.x, end.x),
    yMax = max(start.y, end.y)
)

fun List<Bounds>.flatten() = Bounds(
    xMin = this.minBy { it.xMin }!!.xMin,
    xMax = this.maxBy { it.xMax }!!.xMax,
    yMin = this.minBy { it.yMin }!!.yMin,
    yMax = this.maxBy { it.yMax }!!.yMax
)

fun Line.forEachCoordinate(body: (x: Int, y: Int, l: Int) -> Unit) {
    var length = 0
    body(segments[0].start.x, segments[0].start.y, 0)
    this.segments.forEach { s ->
        (1 .. s.length).forEach { i ->
            body(
                s.start.x + s.direction.dx * i,
                s.start.y + s.direction.dy * i,
                ++length
            )
        }
    }
}

class Grid(val bounds: Bounds) {
    val width = bounds.xMax - bounds.xMin + 1
    val height = bounds.yMax - bounds.yMin + 1

    private val backing = LongArray(width * height) { 0 }

    fun get(x: Int, y: Int): Long {
        return getRaw(x - bounds.xMin, y - bounds.yMin)
    }

    fun getRaw(x: Int, y: Int): Long {
        return backing[(y)*width+(x)]
    }

    fun set(x: Int, y: Int, value: Long) {
        backing[(y-bounds.yMin)*width+(x-bounds.xMin)] = value
    }
}

enum class Direction(val dx: Int, val dy: Int) {
    U(0, -1),
    D(0, 1),
    L(-1, 0),
    R(1, 0)
}

val input by lazy {
    File("Day03.txt").readLines(Charsets.UTF_8)
}

val lines = input.mapIndexed { id, it ->
    val segments = it.split(",")
    var currentX = 0;
    var currentY = 0;
    Line(id, segments.map {
        val length = it.substring(1).toInt()
        val command = enumValueOf<Direction>(it.substring(0, 1))
        LineSegment(
            start = Coord(currentX, currentY),
            end = Coord(currentX + command.dx * length, currentY + command.dy * length),
            direction = command,
            length = length
        ).also {
            currentX += command.dx * length
            currentY += command.dy * length
        }
    })
}

require(lines.size == 2)

val grid = Grid(lines.map { it.bounds() }.flatten())

val intersections = mutableListOf<Intersection>()
lines.forEach {
    val lineValue = it.hashCode()
    it.forEachCoordinate { x, y, l ->
        val currentValue = grid.get(x, y)
        val currentLine = (currentValue shr 32).toInt()
        val currentLength = currentValue.toInt()
        if (currentLength != 0 && currentLine != lineValue) {
            intersections += Intersection(Coord(x, y), currentLength, l)
        }
        grid.set(x, y, (lineValue.toLong() shl 32) or l.toLong())
    }
}

val closestIntersection = intersections
    .filter { it.coord.manhattan() != 0 }
    .minBy { it.coord.manhattan() }!!
println("Assignment A: closest intersection @ ${closestIntersection.coord}, distance: ${closestIntersection.coord.manhattan()}")
require(closestIntersection.coord.manhattan() == 316)

val shortestPath = intersections
    .filter { it.coord.manhattan() != 0 }
    .minBy { it.lengthA + it.lengthB }!!
println("Assignment B: shortest intersection @ ${shortestPath.coord}, length: ${shortestPath.lengthA + shortestPath.lengthB}")
require(shortestPath.lengthA + shortestPath.lengthB == 16368)