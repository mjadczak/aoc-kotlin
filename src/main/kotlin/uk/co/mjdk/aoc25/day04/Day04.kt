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

    fun removing(coords: Iterable<Coord>): Board {
        val indicesToRemove: Set<Int> = coords.mapTo(mutableSetOf()) { it.index ?: error("$it out of bounds") }
        val newCells = cells.mapIndexed { index, cell ->
            if (index in indicesToRemove) {
                check(cell == Cell.Paper) { "$index was not Paper!" }
                Cell.Empty
            } else {
                cell
            }
        }
        return Board(numCols, newCells)
    }

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

fun Board.isAccessiblePaperRoll(coord: Coord): Boolean =
    getOrEmpty(coord) == Cell.Paper && coord.adjacent.count { getOrEmpty(it) == Cell.Paper } < 4

fun main() = aoc(2025, 4, Board::parse) {
    part1 { board ->
        board.allCoords.count { coord ->
            board.isAccessiblePaperRoll(coord)
        }
    }

    part2 { initialBoard ->
        var board = initialBoard
        var removed = 0
        while (true) {
            val toRemove = board.allCoords.filter { board.isAccessiblePaperRoll(it) }.toList()
            if (toRemove.isEmpty()) {
                break
            }
            removed += toRemove.size
            board = board.removing(toRemove)
        }
        removed
    }
}
