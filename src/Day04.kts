val input = (136818 .. 685979)

fun String.toBcdArray() = this.padStart(6, '0').map { it.toInt() - '0'.toInt() }.toIntArray()
fun Int.toBcdArray() = this.toString().toBcdArray()

fun IntArray.incrementBcd(): IntArray {
    var idx = size - 1
    while (idx >= 0) {
        if (this[idx] < 9) {
            this[idx]++
            break
        }
        this[idx] = 0
        idx--
    }
    return this
}

fun IntArray.isValidNumberVariantA() =
    this.sortedArray().contentEquals(this) && this.groupBy { it }.values.any { it.size >= 2 }

fun IntArray.isValidNumberVariantB() =
    this.sortedArray().contentEquals(this) && this.groupBy { it }.values.any { it.size == 2 }

require("122345".toBcdArray().isValidNumberVariantA())
require("111123".toBcdArray().isValidNumberVariantA())
require("111111".toBcdArray().isValidNumberVariantA())
require(!"135679".toBcdArray().isValidNumberVariantA())
require(!"223450".toBcdArray().isValidNumberVariantA())
require(!"123781".toBcdArray().isValidNumberVariantA())

run {
    val count = generateSequence(input.first.toBcdArray()) { it.incrementBcd() }
        .take(input.last - input.first)
        .filter { it.isValidNumberVariantA() }
        .count()

    println("Assignment A: $count")
    require(count == 1919)
}

run {
    val count = generateSequence(input.first.toBcdArray()) { it.incrementBcd() }
        .take(input.last - input.first)
        .filter { it.isValidNumberVariantB() }
        .count()

    println("Assignment B: $count")
    require(count == 1291)
}