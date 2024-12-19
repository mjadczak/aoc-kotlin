package uk.co.mjdk.aoc24.day15

import uk.co.mjdk.aoc.aoc

data class Coord(val row: Int, val col: Int) {
    fun move(direction: Direction): Coord = when (direction) {
        Direction.Up -> copy(row = row - 1)
        Direction.Down -> copy(row = row + 1)
        Direction.Left -> copy(col = col - 1)
        Direction.Right -> copy(col = col + 1)
    }

    val gps: Int
        get() = row * 100 + col
}

enum class Direction {
    Up, Right, Down, Left
}

enum class Cell {
    Space, Wall, Box, Robot
}

class Board(private val data: Array<Cell>, val rows: Int, private var _robot: Coord) {
    val robot: Coord
        get() = _robot

    val cols = run {
        require(data.size % rows == 0)
        data.size / rows
    }

    init {
        require(this[robot] == Cell.Robot)
    }

    private val Coord.index: Int get() = row * cols + col
    private val Int.coord: Coord get() = Coord(this / cols, this % cols)

    fun boxCoords(): Sequence<Coord> = data.indices.asSequence().filter { data[it] == Cell.Box }.map { it.coord }

    operator fun contains(coord: Coord): Boolean = coord.row in (0..<rows) && coord.col in (0..<cols)
    operator fun get(coord: Coord): Cell? = if (!contains(coord)) null else data[coord.index]
    private operator fun set(coord: Coord, cell: Cell) {
        require(coord in this)
        data[coord.index] = cell
    }

    private fun tryMove(coord: Coord, direction: Direction): Boolean {
        val cell = this[coord]
        require(cell == Cell.Box || cell == Cell.Robot)
        val target = coord.move(direction)
        val targetCell = this[target]
        val moved = when (targetCell) {
            Cell.Space -> true
            null, Cell.Wall -> false
            Cell.Box, Cell.Robot -> tryMove(target, direction)
        }
        if (moved) {
            this[target] = cell
            this[coord] = Cell.Space
            if (cell == Cell.Robot) {
                _robot = target
            }
        }
        return moved
    }

    fun moveRobot(direction: Direction) {
        tryMove(robot, direction)
    }

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            var robot: Coord? = null
            val data = lines.flatMapIndexed { row, line ->
                line.mapIndexed { col, char ->
                    when (char) {
                        '#' -> Cell.Wall
                        '.' -> Cell.Space
                        'O' -> Cell.Box
                        '@' -> {
                            check(robot == null)
                            robot = Coord(row, col)
                            Cell.Robot
                        }

                        else -> throw IllegalArgumentException(char.toString())
                    }
                }
            }
            check(robot != null)
            return Board(data.toTypedArray(), lines.size, robot)
        }
    }
}

fun parse(input: String): Pair<Board, List<Direction>> {
    val (fst, snd) = input.split("\n\n")
    val dirs = snd.mapNotNull { ch ->
        when (ch) {
            '^' -> Direction.Up
            '>' -> Direction.Right
            'v' -> Direction.Down
            '<' -> Direction.Left
            else -> null
        }
    }
    return Board.parse(fst) to dirs
}

fun main() = aoc(2024, 15, ::parse) {
    part1 { (board, directions) ->
        directions.forEach { d -> board.moveRobot(d) }
        board.boxCoords().sumOf { it.gps }
    }
}
