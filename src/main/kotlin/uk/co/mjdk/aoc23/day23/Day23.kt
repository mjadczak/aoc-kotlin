package uk.co.mjdk.aoc23.day23

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentSetOf
import uk.co.mjdk.aoc.aoc
import java.util.ArrayDeque
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
            maxDistance = max(maxDistance, st.length)
            val nextVisited = st.visited.add(st.coord)
            tile.allowedDirs.asSequence().map { st.coord + it }.filterNot { it in st.visited }.forEach {
                queue.offer(State(it, nextVisited))
            }
        }


        maxDistance
    }
}
