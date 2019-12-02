import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.max

val input by lazy {
    Files.readAllLines(Paths.get("Day01.txt")).asSequence()
        .map { it.toInt() }
}

fun fuel(mass: Int) = max(0, mass / 3 - 2)

val basicFuelRequirements = input
    .map { fuel(it) }
    .sum()

require(fuel(12) == 2)
require(fuel(14) == 2)
require(fuel(1969) == 654)
require(fuel(100756) == 33583)
println("Assignment A: $basicFuelRequirements")
require(basicFuelRequirements == 3412531)

tailrec fun recursiveFuel(mass: Int, sum: Int = 0): Int = when {
    mass <= 0 -> sum
    else -> recursiveFuel(fuel(mass), sum + fuel(mass))
}

val advancedFuelRequirement = input
    .map { recursiveFuel(it) }
    .sum()

require(recursiveFuel(14) == 2)
require(recursiveFuel(1969) == 966)
require(recursiveFuel(100756) == 50346)
println("Assignment B: $advancedFuelRequirement")
require(advancedFuelRequirement == 5115927)