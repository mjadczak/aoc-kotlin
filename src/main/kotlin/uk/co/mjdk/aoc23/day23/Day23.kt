package uk.co.mjdk.aoc23.day23

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import uk.co.mjdk.aoc.aoc
import java.util.*
import kotlin.math.max

private enum class Dir {
    Up, Right, Down, Left
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = when (dir) {
        Dir.Up -> copy(row = row - 1)
        Dir.Down -> copy(row = row + 1)
        Dir.Left -> copy(col = col - 1)
        Dir.Right -> copy(col = col + 1)
    }

    override fun toString(): String = "($row,$col)"
}

private sealed interface Tile

private data object Path : Tile

private data object Forest : Tile

private data class Slope(val dir: Dir) : Tile

private val Tile.allowedDirs: Set<Dir>
    get() = when (this) {
        Path -> Dir.entries.toSet()
        Forest -> emptySet()
        is Slope -> setOf(dir)
    }

private class Board(private val cells: List<Tile>, val rows: Int) {
    val cols = cells.size / rows
    val start = Coord(0, (0..<cols).first { col -> this[Coord(0, col)] == Path })
    val target = Coord(rows - 1, (0..<cols).first { col -> this[Coord(rows - 1, col)] == Path })

    operator fun get(coord: Coord): Tile? =
        if (coord.col in 0..<cols && coord.row in 0..<rows) cells[coord.row * cols + coord.col]
        else null


    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val cells = lines.flatMap { line ->
                line.map {
                    when (it) {
                        '.' -> Path
                        '#' -> Forest
                        '^' -> Slope(Dir.Up)
                        '>' -> Slope(Dir.Right)
                        'v' -> Slope(Dir.Down)
                        '<' -> Slope(Dir.Left)
                        else -> throw IllegalArgumentException(it.toString())
                    }
                }
            }
            return Board(cells, lines.size)
        }
    }
}


fun main() = aoc(2023, 23, { Board.parse(it) }) {
    // initially, I absolutely missed that we need to reach the end of the board!

    example(
        """
        #.#####################
        #.......#########...###
        #######.#########.#.###
        ###.....#.>.>.###.#.###
        ###v#####.#v#.###.#.###
        ###.>...#.#.#.....#...#
        ###v###.#.#.#########.#
        ###...#.#.#.......#...#
        #####.#.#.#######.#.###
        #.....#.#.#.......#...#
        #.#####.#.#.#########v#
        #.#...#...#...###...>.#
        #.#.#v#######v###.###v#
        #...#.>.#...>.>.#.###.#
        #####v#.#.###v#.#.###.#
        #.....#...#...#.#.#...#
        #.#########.###.#.#.###
        #...###...#...#...#.###
        ###.###.#.###v#####v###
        #...#...#.#.>.>.#.>.###
        #.###.###.#.###.#.#v###
        #.....###...###...#...#
        #####################.#
    """.trimIndent()
    )

    part1 { board ->
        data class State(val coord: Coord, val visited: PersistentSet<Coord>) {
            val length: Int
                get() = visited.size // coord isn't in visited - but the starting square is and it shouldn't count

        }

        val initial = State(board.start, persistentHashSetOf())
        val queue = ArrayDeque<State>()
        queue.offer(initial)
        var maxDistance = 0

        while (queue.isNotEmpty()) {
            val st = queue.poll()
            val tile = board[st.coord]
            if (tile == null || tile == Forest) {
                continue
            }
            if (st.coord == board.target) {
                maxDistance = max(maxDistance, st.length)
            } else {
                val nextVisited = st.visited.add(st.coord)
                tile.allowedDirs.asSequence().map { st.coord + it }.filterNot { it in st.visited }.forEach {
                    queue.offer(State(it, nextVisited))
                }
            }
        }


        maxDistance
    }

    part2 { board ->
        // Large parts of the graph only have one way to go, simplify those down
        // We only store the max distance
        val edges = mutableMapOf<Coord, MutableMap<Coord, Int>>()

        fun addEdge(from: Coord, to: Coord, distance: Int) {
            edges.computeIfAbsent(from) { mutableMapOf() }.let { map -> map[to] = max(map[to] ?: 0, distance) }
            edges.computeIfAbsent(to) { mutableMapOf() }.let { map -> map[from] = max(map[from] ?: 0, distance) }
        }

        fun Coord.neighbours(): Set<Coord> =
            Dir.entries.asSequence().map { this + it }.filter { c -> board[c].let { it == Path || it is Slope } }
                .toSet()

        val poles = (0..<board.rows).asSequence().flatMap { row ->
            (0..<board.cols).mapNotNull { col ->
                val coord = Coord(row, col)
                val tile = board[coord]!!
                if (tile == Forest) {
                    null
                } else {
                    val neighbours = coord.neighbours()
                    if (neighbours.size == 2) {
                        null
                    } else {
                        coord to neighbours
                    }
                }
            }
        }.toMap()
        check(board.start in poles)
        check(board.target in poles)

        // from each pole, we have some set of directions (1 or 3+), and if we follow them we should have a straight path until we encounter another pole

        poles.forEach { (startPole, nextCoords) ->
            nextCoords.forEach { nextCoord ->
                // we could probably just detect the poles inline and do the whole thing with a single DFS - but meh
                // we end up traversing each path both ways, oh well
                var distance = 1
                var last = startPole
                var current = nextCoord
                while (current !in poles) {
                    val next = current.neighbours().first { it != last }
                    last = current
                    current = next
                    distance += 1
                }
                addEdge(startPole, current, distance)
            }
        }

        // now a lil' recursive DFS without needing to store many sets, just for fun
        val visited = linkedSetOf<Coord>()
        var maxDistance = 0

        fun visit(node: Coord, distance: Int) {
            if (node == board.target) {
                maxDistance = max(maxDistance, distance)
                return
            }

            visited.add(node)
            val nextNodes = edges[node]!!.entries.filter { it.key !in visited }
            nextNodes.forEach { (next, dist) ->
                visit(next, distance + dist)
            }
            visited.remove(node)
        }

        visit(board.start, 0)

        maxDistance
    }
}
