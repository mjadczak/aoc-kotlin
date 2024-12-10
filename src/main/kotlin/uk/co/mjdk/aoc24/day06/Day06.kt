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

class Board(private val data: Array<Cell>, val cols: Int, val initialPos: Coord) {
    val rows: Int = run {
        check(data.size % cols == 0)
        data.size / cols
    }

    operator fun get(coord: Coord): Cell? =
        if (coord.col in 0..<cols && coord.row in 0..<rows) data[coord.row * cols + coord.col] else null

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

fun main() = aoc(2024, 6, Board::parse) {
    part1 { board ->
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
        positions.size
    }
}
