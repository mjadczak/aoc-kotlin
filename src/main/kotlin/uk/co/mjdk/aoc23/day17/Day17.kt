package uk.co.mjdk.aoc23.day17

import uk.co.mjdk.aoc.aoc
import java.util.PriorityQueue

private enum class Dir {
    Up, Right, Down, Left;

    fun allowedNext(): Set<Dir> = when (this) {
        Up -> setOf(Up, Left, Right)
        Right -> setOf(Right, Up, Down)
        Down -> setOf(Down, Left, Right)
        Left -> setOf(Left, Down, Up)
    }
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = when (dir) {
        Dir.Up -> copy(row = row - 1)
        Dir.Down -> copy(row = row + 1)
        Dir.Left -> copy(col = col - 1)
        Dir.Right -> copy(col = col + 1)
    }
}


private class Board(private val cells: List<Int>, val rows: Int) {
    val cols = cells.size / rows

    operator fun get(coord: Coord): Int? =
        if (coord.col in 0..<cols && coord.row in 0..<rows) cells[coord.row * cols + coord.col]
        else null


    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val cells = lines.flatMap { line ->
                line.map {
                    it.digitToInt()
                }
            }
            return Board(cells, lines.size)
        }
    }
}

fun main() = aoc(2023, 17, { Board.parse(it) }) {
    // Dijkstra, but each node in the graph needs to keep track of direction of movement + number of straight line moves so far
    // But this means we have multiple potential terminal nodes

    part1 { board ->
        data class State(val coord: Coord, val dirOnEnter: Dir, val numMovedStraightLine: Int) {
            fun neighbours(): List<Pair<State, Int>> = dirOnEnter.allowedNext().mapNotNull { nextDir ->
                State(
                    coord + nextDir,
                    nextDir,
                    if (dirOnEnter == nextDir) numMovedStraightLine + 1 else 1
                ).takeUnless { it.numMovedStraightLine > 3 }
                    ?.let { nextState -> board[nextState.coord]?.let { nextState to it } }
            }
        }

        val initial = State(Coord(0, 0), Dir.Right, 0)

        val dist = mutableMapOf(initial to 0).withDefault { Int.MAX_VALUE }
        val visited = mutableSetOf<State>()
        val queue = PriorityQueue<State>(compareBy { dist.getValue(it) }).also { it.add(initial) }

        while (queue.isNotEmpty()) {
            val thisNode = queue.poll()
            // TODO could use a variant with a decrease_priority function
            visited.add(thisNode)

            thisNode.neighbours().filterNot { it.first in visited }.forEach { (nextState, cost) ->
                val alt = dist.getValue(thisNode).also { check(it != Int.MAX_VALUE) } + cost
                if (alt < dist.getValue(nextState)) {
                    dist[nextState] = alt
                    queue.remove(nextState)
                    queue.offer(nextState)
                }
            }
        }

        val terminals = dist.entries.filter { it.key.coord == Coord(board.rows - 1, board.cols - 1) }

        terminals.minOf { it.value }
    }
}
