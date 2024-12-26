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

    fun widen(): WideBoard {
        val newData = data.asSequence().flatMap { cell ->
            when (cell) {
                Cell.Space -> listOf(WideCell.Space, WideCell.Space)
                Cell.Wall -> listOf(WideCell.Wall, WideCell.Wall)
                Cell.Box -> listOf(WideCell.BoxL, WideCell.BoxR)
                Cell.Robot -> listOf(WideCell.Robot, WideCell.Space)
            }
        }.toList().toTypedArray()

        return WideBoard(newData, rows, robot.copy(col = robot.col * 2))
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

enum class WideCell {
    Wall, Space, BoxL, BoxR, Robot
}

class WideBoard(private val data: Array<WideCell>, val rows: Int, private var _robot: Coord) {
    val robot: Coord
        get() = _robot

    val cols = run {
        require(data.size % rows == 0)
        data.size / rows
    }

    init {
        require(this[robot] == WideCell.Robot)
    }

    private val Coord.index: Int get() = row * cols + col
    private val Int.coord: Coord get() = Coord(this / cols, this % cols)

    fun boxCoords(): Sequence<Coord> = data.indices.asSequence().filter { data[it] == WideCell.BoxL }.map { it.coord }

    operator fun contains(coord: Coord): Boolean = coord.row in (0..<rows) && coord.col in (0..<cols)
    operator fun get(coord: Coord): WideCell? = if (!contains(coord)) null else data[coord.index]
    private operator fun set(coord: Coord, cell: WideCell) {
        require(coord in this)
        data[coord.index] = cell
    }

    private fun tryMove(coord: Coord, direction: Direction, checkPartner: Boolean = true): Boolean {
        val cell = this[coord]
        require(cell == WideCell.BoxR || cell == WideCell.BoxL || cell == WideCell.Robot)
        val target = coord.move(direction)
        // really not the most efficient, but bleh
        var checkpoint: Array<WideCell>? = null
        val partnerMoved =
            if (checkPartner && (cell == WideCell.BoxL || cell == WideCell.BoxR) && (direction == Direction.Up || direction == Direction.Down)) {
                checkpoint = data.copyOf()
                val partner = when (cell) {
                    WideCell.BoxR -> {
                        val tp = coord.move(Direction.Left)
                        check(this[tp] == WideCell.BoxL)
                        tp
                    }

                    WideCell.BoxL -> {
                        val tp = coord.move(Direction.Right)
                        check(this[tp] == WideCell.BoxR)
                        tp
                    }

                    else -> throw IllegalStateException(cell.toString())
                }
                tryMove(partner, direction, false)
            } else {
                true
            }
        val targetCell = this[target]
        val moved = partnerMoved && when (targetCell) {
            WideCell.Space -> true
            null, WideCell.Wall -> false
            WideCell.BoxL, WideCell.BoxR, WideCell.Robot -> tryMove(target, direction)
        }
        if (moved) {
            this[target] = cell
            this[coord] = WideCell.Space
            if (cell == WideCell.Robot) {
                _robot = target
            }
        } else {
            checkpoint?.copyInto(data)
        }
        return moved
    }

    fun findFirstInvalidPosition(): Coord? = data.withIndex().find {
        val coord = it.index.coord
        (it.value == WideCell.BoxL && get(coord.move(Direction.Right)) != WideCell.BoxR) ||
                (it.value == WideCell.BoxR && get(coord.move(Direction.Left)) != WideCell.BoxL)
    }?.index?.coord

    fun moveRobot(direction: Direction) {
        tryMove(robot, direction)
    }

    fun render(): String = buildString {
        (0..<rows).forEach { row ->
            (0..<cols).forEach { col ->
                when (get(Coord(row, col))) {
                    WideCell.Wall -> '#'
                    WideCell.Space -> '.'
                    WideCell.BoxL -> '['
                    WideCell.BoxR -> ']'
                    WideCell.Robot -> '@'
                    null -> throw IllegalStateException()
                }.let { append(it) }
            }
            appendLine()
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

    part2 { (board, directions) ->
        val wideBoard = board.widen()
        directions.forEachIndexed { idx, d ->
            wideBoard.moveRobot(d)
            wideBoard.findFirstInvalidPosition()?.let {
                throw IllegalStateException("Invalid position found at $it")
            }
        }
        wideBoard.boxCoords().sumOf { it.gps }
    }
}
