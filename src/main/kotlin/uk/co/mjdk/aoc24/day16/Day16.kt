package uk.co.mjdk.aoc24.day16

import uk.co.mjdk.aoc.aoc
import java.util.PriorityQueue
import kotlin.collections.filterNot
import kotlin.collections.forEach

data class Coord(val row: Int, val col: Int) {
    fun move(direction: Direction): Coord = when (direction) {
        Direction.North -> copy(row = row - 1)
        Direction.South -> copy(row = row + 1)
        Direction.West -> copy(col = col - 1)
        Direction.East -> copy(col = col + 1)
    }
}

enum class Direction {
    North, East, South, West
}

fun Direction.turnCw(): Direction = when (this) {
    Direction.North -> Direction.East
    Direction.East -> Direction.South
    Direction.South -> Direction.West
    Direction.West -> Direction.North
}

fun Direction.turnCcw(): Direction = when (this) {
    Direction.North -> Direction.West
    Direction.West -> Direction.South
    Direction.South -> Direction.East
    Direction.East -> Direction.North
}

enum class Cell {
    Space, Wall
}

data class State(val pos: Coord, val dir: Direction)

fun State.forward(): State = copy(pos = pos.move(dir))
fun State.turnCw(): State = copy(dir = dir.turnCw())
fun State.turnCcw(): State = copy(dir = dir.turnCcw())

fun State.next(): Sequence<Pair<State, Int>> = sequence {
    yield(forward() to 1)
    yield(turnCw() to 1000)
    yield(turnCcw() to 1000)
}

class Board(private val data: Array<Cell>, val rows: Int, val start: Coord, val end: Coord) {
    val cols = run {
        require(data.size % rows == 0)
        data.size / rows
    }

    init {
        require(start in this)
        require(end in this)
    }

    private val Coord.index: Int get() = row * cols + col
    private val Int.coord: Coord get() = Coord(this / cols, this % cols)

    operator fun contains(coord: Coord): Boolean = coord.row in (0..<rows) && coord.col in (0..<cols)
    operator fun get(coord: Coord): Cell? = if (!contains(coord)) null else data[coord.index]

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            var start: Coord? = null
            var end: Coord? = null
            val data = lines.flatMapIndexed { row, line ->
                line.mapIndexed { col, char ->
                    when (char) {
                        '#' -> Cell.Wall
                        '.' -> Cell.Space
                        'S' -> {
                            check(start == null)
                            start = Coord(row, col)
                            Cell.Space
                        }

                        'E' -> {
                            check(end == null)
                            end = Coord(row, col)
                            Cell.Space
                        }

                        else -> throw IllegalArgumentException(char.toString())
                    }
                }
            }
            check(start != null)
            check(end != null)
            return Board(data.toTypedArray(), lines.size, start, end)
        }
    }
}

fun main() = aoc(2024, 16, Board::parse) {
    part1 { board ->
        val initial = State(board.start, Direction.East)
        val dist = mutableMapOf(initial to 0).withDefault { Int.MAX_VALUE }
        val visited = mutableSetOf<State>()
        val queue = PriorityQueue<State>(compareBy { dist.getValue(it) }).also { it.add(initial) }

        fun State.neighbours(): Sequence<Pair<State, Int>> = next().filter { board[it.first.pos] == Cell.Space }
        while (queue.isNotEmpty()) {
            val thisNode = queue.poll()
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

        val terminals = dist.entries.filter { it.key.pos == board.end }

        terminals.minOf { it.value }
    }
}
