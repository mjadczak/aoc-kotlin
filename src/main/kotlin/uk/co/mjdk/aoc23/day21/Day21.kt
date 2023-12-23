package uk.co.mjdk.aoc23.day21

import uk.co.mjdk.aoc.aoc

private enum class Dir {
    Up, Right, Down, Left;
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = when (dir) {
        Dir.Up -> copy(row = row - 1)
        Dir.Down -> copy(row = row + 1)
        Dir.Left -> copy(col = col - 1)
        Dir.Right -> copy(col = col + 1)
    }
}

private enum class Tile {
    Garden, Rock;
}

private class Board(private val cells: List<Tile>, val rows: Int, val start: Coord) {
    val cols = cells.size / rows

    operator fun get(coord: Coord): Tile? =
        if (coord.col in 0..<cols && coord.row in 0..<rows) cells[coord.row * cols + coord.col]
        else null


    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            var start: Coord? = null
            val cells = lines.flatMapIndexed { row, line ->
                line.mapIndexed { col, char ->
                    when (char) {
                        '.' -> Tile.Garden
                        '#' -> Tile.Rock
                        'S' -> {
                            start = Coord(row, col)
                            Tile.Garden
                        }

                        else -> throw IllegalArgumentException(char.toString())
                    }
                }
            }
            return Board(cells, lines.size, start ?: throw IllegalArgumentException("could not find start"))
        }
    }
}

private data class State(val stepsTaken: Int, val coord: Coord)

private class Calculator(val board: Board, val targetSteps: Int) {
    private val memo = mutableMapOf<State, Set<Coord>>()

    fun findPositions(state: State): Set<Coord> = memo[state] ?: calcPositions(state).also { memo[state] = it }

    private fun calcPositions(state: State): Set<Coord> {
        if (state.stepsTaken == targetSteps) {
            return setOf(state.coord)
        }

        return Dir.entries.asSequence().map { state.coord + it }.filter { board[it] == Tile.Garden }
            .map { findPositions(State(state.stepsTaken + 1, it)) }.reduce { l, r -> l.union(r) }
    }
}

fun main() = aoc(2023, 21, { Board.parse(it) }) {
    part1 { board ->
        Calculator(board, 64).findPositions(State(0, board.start)).size
    }
}
