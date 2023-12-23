package uk.co.mjdk.aoc23.day21

import uk.co.mjdk.aoc.aoc
import java.util.ArrayDeque

private fun (Long).remainder(other: Long): Long = (this % other + other) % other

private enum class Dir {
    Up, Right, Down, Left;
}

private data class Coord(val row: Long, val col: Long) {
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

private data class Board(
    private val cells: List<Tile>, val rows: Int, val start: Coord, val infinite: Boolean = false
) {
    val cols = cells.size / rows

    operator fun get(coord: Coord): Tile? = if (infinite) {
        cells[Math.floorMod(coord.row, rows) * cols + Math.floorMod(coord.col, cols)]
    } else {
        if (coord.col in 0..<cols && coord.row in 0..<rows) cells[coord.row.toInt() * cols + coord.col.toInt()]
        else null
    }


    fun asInfinite(): Board = copy(infinite = true)

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
                            start = Coord(row.toLong(), col.toLong())
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

private val (Coord).parity: Boolean
    get() = (row + col) % 2 != 0L

private val (Int).parity: Boolean
    get() = this % 2 == 1

private fun (Triple<Int, Int, Int>).colourPrint(char: Char) {
    val (r, g, b) = this
    print("\u001b[38;2;")
    print(r)
    print(";")
    print(g)
    print(";")
    print(b)
    print("m")
    print(char)
    print("\u001B[0m")
}

private val red = Triple(255, 0, 0)
private val grey = Triple(150, 150, 150)
private val green = Triple(0, 255, 0)

private fun (Board).findNumPositions(targetSteps: Int, debug: Boolean = false): Int {
    // parity argument: we can end on any square 1) we can reach 2) has the right parity (checkerboard colour)
    val targetParity = start.parity.let {
        if (targetSteps.parity) !it else it
    }

    val reachable = mutableSetOf<Coord>()
    val queue = ArrayDeque<State>()
    queue.offer(State(0, start))
    reachable.add(start)
    while (queue.isNotEmpty()) {
        val st = queue.poll()

        for (dir in Dir.entries) {
            val nextCoord = st.coord + dir
            if (nextCoord in reachable) continue

            if (this[nextCoord] == Tile.Garden) {
                reachable.add(nextCoord)
                val nextState = State(st.stepsTaken + 1, nextCoord)
                if (nextState.stepsTaken == targetSteps) continue
                queue.offer(nextState)
            }
        }
    }

    if (debug) {
        println("NUM STEPS $targetSteps")
        val diameter = 50
        val rowr = (start.row.toInt() - diameter)..(start.row.toInt() + diameter)
        val colr = (start.col.toInt() - diameter)..(start.col.toInt() + diameter)
        colr.forEach { col ->
            print(if (col == 0) 'v' else ' ')
        }
        println()
        rowr.forEach { row ->
            print(if (row == 0) '>' else ' ')
            colr.forEach { col ->
                val coord = Coord(row.toLong(), col.toLong())
                if (coord in reachable && coord.parity == targetParity) {
                    green.colourPrint(if (coord == start) 'S' else 'O')
                } else if (coord in reachable) {
                    grey.colourPrint(if (coord == start) 's' else 'o')
                } else if (this[coord] == Tile.Rock) red.colourPrint('#')
                else grey.colourPrint('.')
            }
            println()
        }

        println()
    }

    return reachable.count { it.parity == targetParity }
}

fun main() = aoc(2023, 21, { Board.parse(it) }) {
    example(
        """
        ...........
        .....###.#.
        .###.##..#.
        ..#.#...#..
        ....#.#....
        .##..S####.
        .##..#...#.
        .......##..
        .##.#.####.
        .##..##.##.
        ...........
    """.trimIndent()
    )

    part1 { board ->
        board.findNumPositions(64)
    }

    part2 { srcBoard ->
        val board = srcBoard.asInfinite()

        val target = 26_501_365

        // meh - special structure of input lends itself to this solution
        val size = board.cols
        check(board.rows == size)
        val edge = size / 2
        // this ends up a quadratic - so grab the three points and calculate the solution at the end
        val y = (0..2).map { i -> edge + i * size }.map { board.findNumPositions(it).toLong() }
        val c = y[0]
        val a = (y[2] - (2 * y[1]) + c) / 2
        val b = y[1] - c - a

        val x = (target - edge) / size
        check((target - edge) % size == 0)

        println("y=$y, a=$a, b=$b, c=$c, x=$x")

        (a * x * x) + (b * x) + c
    }
}
