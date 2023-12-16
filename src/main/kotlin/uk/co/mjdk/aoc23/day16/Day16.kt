package uk.co.mjdk.aoc23.day16

import uk.co.mjdk.aoc.aoc

private enum class Dir {
    Up,
    Right,
    Down,
    Left,
}

private enum class Cell {
    Empty,
    MirrorR, // /
    MirrorL, // \
    SplitterV,
    SplitterH;

    fun nextDirs(fromDir: Dir): List<Dir> = when (this) {
        Empty -> listOf(fromDir)
        MirrorR -> when (fromDir) {
            Dir.Up -> Dir.Right
            Dir.Right -> Dir.Up
            Dir.Down -> Dir.Left
            Dir.Left -> Dir.Down
        }.let(::listOf)

        MirrorL -> when (fromDir) {
            Dir.Up -> Dir.Left
            Dir.Left -> Dir.Up
            Dir.Down -> Dir.Right
            Dir.Right -> Dir.Down
        }.let(::listOf)

        SplitterV -> if (fromDir == Dir.Left || fromDir == Dir.Right) listOf(Dir.Up, Dir.Down) else listOf(fromDir)
        SplitterH -> if (fromDir == Dir.Up || fromDir == Dir.Down) listOf(Dir.Left, Dir.Right) else listOf(fromDir)
    }
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = when (dir) {
        Dir.Up -> Coord(row - 1, col)
        Dir.Right -> Coord(row, col + 1)
        Dir.Down -> Coord(row + 1, col)
        Dir.Left -> Coord(row, col - 1)
    }
}

private class Board(private val cells: List<Cell>, val rows: Int) {
    val cols = cells.size / rows

    init {
        require(cols * rows == cells.size)
    }

    operator fun get(coord: Coord): Cell? =
        if (coord.row in 0..<rows && coord.col in 0..<cols) cells.get(coord.row * cols + coord.col) else null

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val cells = lines.flatMap { line ->
                line.map {
                    when (it) {
                        '.' -> Cell.Empty
                        '/' -> Cell.MirrorR
                        '\\' -> Cell.MirrorL
                        '|' -> Cell.SplitterV
                        '-' -> Cell.SplitterH
                        else -> throw IllegalArgumentException(it.toString())
                    }
                }
            }
            return Board(cells, lines.size)
        }
    }
}

fun main() = aoc(2023, 16, { Board.parse(it) }) {
    example(
        """
        .|...\....
        |.-.\.....
        .....|-...
        ........|.
        ..........
        .........\
        ..../.\\..
        .-.-/..|..
        .|....-|.\
        ..//.|....
    """.trimIndent()
    )

    part1 { board ->
        data class State(val coord: Coord, val dir: Dir)

        val visitedCells = mutableSetOf<Coord>()
        val visitedStates = mutableSetOf<State>()

        val initial = State(Coord(0, -1), Dir.Right)
        val queue = ArrayDeque<State>().apply { add(initial) }

        while (queue.isNotEmpty()) {
            val state = queue.removeFirst()
            val nextCoord = state.coord + state.dir
            board[nextCoord]?.let { cell ->
                visitedCells.add(nextCoord)
                cell.nextDirs(state.dir).asSequence().map { State(nextCoord, it) }.filterNot { it in visitedStates }
                    .forEach {
                        visitedStates.add(it)
                        queue.add(it)
                    }
            }
        }

//        (0..<board.rows).forEach { row ->
//            (0..<board.cols).forEach { col ->
//                if (Coord(row, col) in visitedCells) print("#") else print(".")
//            }
//            println()
//        }

        visitedCells.size
    }
}
