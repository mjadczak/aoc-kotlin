package uk.co.mjdk.aoc23.day14

import uk.co.mjdk.aoc.aoc

private data class Coord(val row: Int, val col: Int)

private enum class Cell {
    Empty,
    RoundRock,
    CubeRock
}

private interface BoardLike {
    val rows: Int
    val cols: Int
    operator fun get(coord: Coord): Cell
    fun slide(): Board
}

private fun BoardLike.rockChunks(col: Int): Sequence<Pair<Int, Int>> = sequence {
    var chunkStartRow = 0
    var roundRocksThisChunk = 0
    for (coord in (0..<rows).map { Coord(it, col) }) {
        val cell = this@rockChunks[coord]
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

private fun BoardLike.pretty(): String = buildString {
    (0..<rows).forEach { row ->
        (0..<cols).forEach { col ->
            append(
                when (get(Coord(row, col))) {
                    Cell.Empty -> '.'
                    Cell.CubeRock -> '#'
                    Cell.RoundRock -> 'O'
                }
            )
        }
        appendLine()
    }
}

// query coord -> north coord; essentially rotations
private enum class SlideDirection(val xform: (Int, Int, Coord) -> Coord, val flip: Boolean) {
    North({ _, _, coord -> coord }, false),
    East({ _, cols, coord -> Coord(coord.col, cols - 1 - coord.row) }, true),
    South({ rows, cols, coord -> Coord(rows - 1 - coord.row, cols - 1 - coord.col) }, false),
    West({ rows, _, coord -> Coord(rows - 1 - coord.col, coord.row) }, true)
}

private data class Board(private val cells: List<Cell>, override val rows: Int) : BoardLike {
    override val cols: Int = cells.size / rows

    init {
        require(cells.size == rows * cols)
    }

    private val Coord.index: Int
        get() = row * cols + col

    override operator fun get(coord: Coord): Cell = cells[coord.index]

    override fun slide(): Board = rotated(SlideDirection.North).slide()

    private inner class Rotated(private val direction: SlideDirection) : BoardLike {
        override val rows: Int
            get() = if (direction.flip) this@Board.cols else this@Board.rows

        override val cols: Int
            get() = if (direction.flip) this@Board.rows else this@Board.cols

        private val Coord.transformed: Coord
            get() = direction.xform(this@Board.rows, this@Board.cols, this)

        override fun get(coord: Coord): Cell = this@Board[coord.transformed]

        override fun slide(): Board {
            // copy list without round rocks, then fill in the round rocks in the right places
            val newCells = this@Board.cells.mapTo(mutableListOf()) { if (it == Cell.RoundRock) Cell.Empty else it }
            fun setRock(coord: Coord) {
                newCells[coord.transformed.index] = Cell.RoundRock
            }
            (0..<cols).forEach { col ->
                rockChunks(col).forEach { (startRowIdx, numRocks) ->
                    (startRowIdx..<startRowIdx + numRocks).forEach { row ->
                        setRock(Coord(row, col))
                    }
                }
            }
            return Board(newCells, this@Board.rows)
        }
    }

    fun rotated(direction: SlideDirection): BoardLike = Rotated(direction)

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val rows = lines.size
            val cells = lines.flatMap {
                it.map { char ->
                    when (char) {
                        '.' -> Cell.Empty
                        'O' -> Cell.RoundRock
                        '#' -> Cell.CubeRock
                        else -> throw IllegalArgumentException(char.toString())
                    }
                }
            }
            return Board(cells, rows)
        }
    }
}

private fun sumBetween(a: Int, b: Int): Int = (b - a + 1) * (a + b) / 2

fun main() = aoc(2023, 14, { Board.parse(it) }) {

    example(
        """
        O....#....
        O.OO#....#
        .....##...
        OO.#O....O
        .O.....O#.
        O.#..O.#.#
        ..O..#O..O
        .......O..
        #....###..
        #OO..#....
    """.trimIndent()
    )

    part1 { board ->
        (0..<board.cols).sumOf { col ->
            board.rockChunks(col).sumOf { (startRowIdx, numRocks) ->
                val endPoints = board.rows - startRowIdx
                val startPoints = endPoints - numRocks + 1
                sumBetween(startPoints, endPoints)
            }
        }
    }

    part2 { initialBoard ->
        fun cycle(board: Board): Board =
            board.slide()
                .rotated(SlideDirection.West).slide()
                .rotated(SlideDirection.South).slide()
                .rotated(SlideDirection.East).slide()

        val (offset, cycle) = run {
            val boards = generateSequence(initialBoard, ::cycle)
            val seenBoards = mutableMapOf<Board, Int>()
            val boardList = mutableListOf<Board>()

            for ((idx, board) in boards.withIndex()) {
                seenBoards[board]?.let { prevIdx -> return@run prevIdx to boardList.drop(prevIdx) }
                seenBoards[board] = idx
                boardList.add(board)
            }

            throw IllegalStateException()
        }

        val idx = (1_000_000_000 - offset) % cycle.size
        val finalBoard = cycle[idx]

        (0..<finalBoard.rows).sumOf { row ->
            (0..<finalBoard.cols).sumOf { col ->
                if (finalBoard[Coord(row, col)] == Cell.RoundRock) {
                    finalBoard.rows - row
                } else {
                    0
                }
            }
        }
    }
}
