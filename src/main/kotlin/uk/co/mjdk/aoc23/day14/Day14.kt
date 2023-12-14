package uk.co.mjdk.aoc23.day14

import uk.co.mjdk.aoc.aoc

private data class Coord(val row: Int, val col: Int)

private enum class Cell {
    Empty,
    RoundRock,
    CubeRock
}

private class Board(input: String) {
    private val cells: List<Cell>
    val rows: Int
    val cols: Int

    init {
        val lines = input.lines()
        rows = lines.size
        cols = lines[0].length
        cells = lines.flatMap {
            it.map { char ->
                when (char) {
                    '.' -> Cell.Empty
                    'O' -> Cell.RoundRock
                    '#' -> Cell.CubeRock
                    else -> throw IllegalArgumentException(char.toString())
                }
            }
        }
    }

    operator fun get(coord: Coord): Cell = cells[coord.row * cols + coord.col]
}

private fun sumBetween(a: Int, b: Int): Int = (b - a + 1) * (a + b) / 2

fun main() = aoc(2023, 14, { Board(it) }) {
    part1 { board ->
        // go col by col, chunk by cube rocks, count num round rocks in each chunk, keep track of start index
        (0..<board.cols).sumOf { col ->
            val chunks = sequence {
                var chunkStartRow = 0
                var roundRocksThisChunk = 0
                for (coord in (0..<board.rows).map { Coord(it, col) }) {
                    val cell = board[coord]
                    if (cell == Cell.RoundRock) {
                        roundRocksThisChunk += 1
                    } else if (cell == Cell.CubeRock) {
                        if (roundRocksThisChunk > 0) {
                            yield(chunkStartRow to roundRocksThisChunk)
                        }
                        chunkStartRow = coord.row + 1
                        roundRocksThisChunk = 0
                    }
                }
                if (roundRocksThisChunk > 0) {
                    yield(chunkStartRow to roundRocksThisChunk)
                }
            }

            chunks.sumOf { (startRowIdx, numRocks) ->
                val endPoints = board.rows - startRowIdx
                val startPoints = endPoints - numRocks + 1
                sumBetween(startPoints, endPoints)
            }
        }
    }

}
