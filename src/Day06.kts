import java.io.File

val input by lazy {
    File("Day06.txt").readLines(Charsets.UTF_8)
}

val edges = input.map { line ->
    val nodes = line.split(")")
    nodes[1] to nodes[0]
}.toMap()

fun pathToRoot(node: String) = generateSequence(node) { edges[it] }
val orbits = edges.keys.map { pathToRoot(it).count() - 1 }.sum()

println("Assignment A: $orbits")

val santaPath = pathToRoot("SAN").toList()
val youPath = pathToRoot("YOU").toList()
val crossing = santaPath.intersect(youPath).count() // Find index of divergence
val transfers = santaPath.size + youPath.size - 2 * crossing - 2 // do not count YOU/SAN

println("Assignment B: $transfers")

//### Silly Dijkstra version for posterity.
//data class Path(val parent: Path?, val node: String, val cost: Int)
//
//fun dijkstra(edgeMap: Map<String, String>, start: String, end: String): Path? {
//    fun children(node: String) = edgeMap.filterKeys { it == node }.values + edgeMap.filterValues { it == node }.keys
//
//    val visited = mutableMapOf<String, Path>()
//    val queue = PriorityQueue<Path> { a, b -> a.cost.compareTo(b.cost) }
//    queue += Path(null, start, 0)
//
//    while (!queue.isEmpty()) {
//        val current = queue.poll()
//        if (current.node == end) {
//            return current
//        }
//        val children = children(current.node)
//        children.filter { it != current.parent?.node }.forEach { c ->
//            val cost = current.cost + 1
//            val existing = visited[c]
//            if (existing == null || cost < existing.cost) {
//                val path = Path(current, c, current.cost + 1)
//                visited[c] = path
//                queue += path
//            }
//        }
//    }
//
//    return null
//}
//
//val path = dijkstra(edges, "YOU", "SAN")
//val transfers = path!!.cost - 2
