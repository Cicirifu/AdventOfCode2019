import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.io.File
import java.lang.StringBuilder
import java.lang.UnsupportedOperationException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun main() {
    data class Coord(val x: Int, val y: Int)
    data class LineSegment(val id: Int, val start: Coord, val end: Coord)

    class Grid(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) {
        val width = xMax - xMin + 1
        val height = yMax - yMin + 1

        private val backing = IntArray(width * height) { 0 }

        fun get(x: Int, y: Int): Int {
            return getRaw(x - xMin, y - yMin)
        }

        fun getRaw(x: Int, y: Int): Int {
            return backing[(y)*width+(x)]
        }

        fun set(x: Int, y: Int, value: Int) {
            backing[(y-yMin)*width+(x-xMin)] = value
        }
    }

    val input by lazy {
        File("Day03.txt").readLines(Charsets.UTF_8)
    }

    val segments = input.mapIndexed { id, it ->
        val segments = it.split(",")
        var currentX = 0;
        var currentY = 0;
        segments.map {
            val length = it.substring(1).toInt()
            when (it[0]) {
                'U' -> {
                    LineSegment(id, Coord(currentX, currentY), Coord(currentX, currentY - length)).also {
                        currentY -= length
                    }
                }
                'D' -> {
                    LineSegment(id, Coord(currentX, currentY), Coord(currentX, currentY + length)).also {
                        currentY += length
                    }
                }
                'L' -> {
                    LineSegment(id, Coord(currentX, currentY), Coord(currentX - length, currentY)).also {
                        currentX -= length
                    }
                }
                'R' -> {
                    LineSegment(id, Coord(currentX, currentY), Coord(currentX + length, currentY)).also {
                        currentX += length
                    }
                }
                else -> throw UnsupportedOperationException()
            }
        }
    }.flatten()

    val xMax = segments.map { max(it.start.x, it.end.x) }.max() ?: 0
    val yMax = segments.map { max(it.start.y, it.end.y) }.max() ?: 0
    val xMin = segments.map { min(it.start.x, it.end.x) }.min() ?: 0
    val yMin = segments.map { min(it.start.y, it.end.y) }.min() ?: 0

    val grid = Grid(xMin, xMax, yMin, yMax)

    fun drawOr(grid: Grid, seg: LineSegment): List<Coord> {
        val segmentValue = (1 shl seg.id)
        return (min(seg.start.x, seg.end.x) .. max(seg.start.x, seg.end.x)).flatMap { x ->
            (min(seg.start.y, seg.end.y) .. max(seg.start.y, seg.end.y)).mapNotNull { y ->
                val currentValue = grid.get(x, y)
                grid.set(x, y,  segmentValue)
                if (currentValue or segmentValue == segmentValue) null else Coord(x, y)
            }
        }
    }

    val intersections = segments.flatMap { drawOr(grid, it) }

    val closest = intersections.map { abs(it.x) + abs(it.y) }.filter { it != 0 }.min()
    println(closest)

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
