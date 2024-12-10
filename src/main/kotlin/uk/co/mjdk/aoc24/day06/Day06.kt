package uk.co.mjdk.aoc24.day06

import uk.co.mjdk.aoc.aoc

data class Coord(val row: Int, val col: Int) {
    operator fun plus(other: Direction) = when (other) {
        Direction.Up -> copy(row = row - 1)
        Direction.Down -> copy(row = row + 1)
        Direction.Left -> copy(col = col - 1)
        Direction.Right -> copy(col = col + 1)
    }
}

enum class Direction { Left, Right, Up, Down }

val Direction.rightTurn: Direction
    get() = when (this) {
        Direction.Up -> Direction.Right
        Direction.Right -> Direction.Down
        Direction.Down -> Direction.Left
        Direction.Left -> Direction.Up
    }

data class Guard(val pos: Coord, val dir: Direction) {
    fun forward(): Guard = copy(pos = pos + dir)
    fun turn(): Guard = copy(dir = dir.rightTurn)
}

enum class Cell {
    Blocked,
    Free,
}

class Board(private val data: Array<Cell>, val cols: Int, val initialPos: Coord, val extraBlocked: Coord? = null) {
    val rows: Int = run {
        check(data.size % cols == 0)
        data.size / cols
    }

    operator fun get(coord: Coord): Cell? = when {
        coord == extraBlocked -> Cell.Blocked
        coord.col in 0..<cols && coord.row in 0..<rows -> data[coord.row * cols + coord.col]
        else -> null
    }

    fun withBlocked(extraBlocked: Coord) = Board(data, cols, initialPos, extraBlocked)

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val cols = lines[0].length
            var initialPos: Coord? = null
            val data = lines.flatMapIndexed { row, l ->
                l.mapIndexed { col, ch ->
                    when (ch) {
                        '#' -> Cell.Blocked
                        '.' -> Cell.Free
                        '^' -> {
                            check(initialPos == null)
                            initialPos = Coord(row, col)
                            Cell.Free
                        }

                        else -> throw IllegalStateException("Unexpected '$ch'")
                    }
                }
            }.toTypedArray()
            check(initialPos != null)
            return Board(data, cols, initialPos)
        }
    }
}

fun distinctPositions(board: Board): Set<Coord> {
    var state = Guard(board.initialPos, Direction.Up)
    val positions = mutableSetOf<Coord>()
    while (true) {
        positions.add(state.pos)
        val forwardState = state.forward()
        state = when (board[forwardState.pos]) {
            null -> break
            Cell.Free -> forwardState
            Cell.Blocked -> state.turn()
        }
    }
    return positions
}

fun checkIsLoop(board: Board): Boolean {
    var state = Guard(board.initialPos, Direction.Up)
    val states = mutableSetOf<Guard>()
    while (true) {
        if (state in states) return true
        states.add(state)
        val forwardState = state.forward()
        state = when (board[forwardState.pos]) {
            null -> break
            Cell.Free -> forwardState
            Cell.Blocked -> state.turn()
        }
    }
    return false
}

fun main() = aoc(2024, 6, Board::parse) {
    part1 { board ->
        distinctPositions(board).size
    }

    part2 { board ->
        val candidates = distinctPositions(board)
        candidates.filterNot { it == board.initialPos }.filter { checkIsLoop(board.withBlocked(it)) }.size
    }
}
