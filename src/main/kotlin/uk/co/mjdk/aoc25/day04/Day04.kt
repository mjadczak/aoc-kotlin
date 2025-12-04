package uk.co.mjdk.aoc25.day04

import uk.co.mjdk.aoc.aoc

data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = Coord(row + dir.rowDelta, col + dir.colDelta)

    val adjacent: Sequence<Coord> = Dir.entries.asSequence().map { this + it }
}

enum class Dir(val rowDelta: Int, val colDelta: Int) {
    Up(-1, 0),
    UpRight(-1, 1),
    Right(0, 1),
    DownRight(1, 1),
    Down(1, 0),
    DownLeft(1, -1),
    Left(0, -1),
    UpLeft(-1, -1);
}

enum class Cell {
    Paper,
    Empty;
}

class Board(val numCols: Int, private val cells: List<Cell>) {
    val numRows: Int = run {
        check(cells.size % numCols == 0)
        cells.size / numCols
    }

    val rowIndices = 0..<numRows
    val colIndices = 0..<numCols
    val allCoords =
        rowIndices.asSequence().flatMap { row ->
            colIndices.asSequence().map { col ->
                Coord(row, col)
            }
        }

    private val Coord.indexUnsafe: Int get() = row * numRows + col
    private val Coord.index: Int? get() = if (contains(this)) indexUnsafe else null

    operator fun contains(coord: Coord): Boolean = coord.row in rowIndices && coord.col in colIndices
    operator fun get(coord: Coord): Cell? = coord.index?.let { cells[it] }
    fun getOrEmpty(coord: Coord): Cell = get(coord) ?: Cell.Empty

    companion object {
        fun parse(input: String): Board {
            val lines = input.lineSequence()
            val numCols = lines.first().trim().length
            val cells = lines.flatMap {
                it.map { c ->
                    when (c) {
                        '.' -> Cell.Empty
                        '@' -> Cell.Paper
                        else -> error(c)
                    }
                }
            }.toList()
            return Board(numCols, cells)
        }
    }
}

fun main() = aoc(2025, 4, Board::parse) {
    part1 { board ->
        board.allCoords.count { coord ->
            board.getOrEmpty(coord) == Cell.Paper && coord.adjacent.count { board.getOrEmpty(it) == Cell.Paper } < 4
        }
    }
}
