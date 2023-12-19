package uk.co.mjdk.aoc23.day18

import uk.co.mjdk.aoc.aoc
import java.util.ArrayDeque

private enum class Dir {
    Up,
    Right,
    Down,
    Left
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = when (dir) {
        Dir.Up -> copy(row = row - 1)
        Dir.Down -> copy(row = row + 1)
        Dir.Right -> copy(col = col + 1)
        Dir.Left -> copy(col = col - 1)
    }
}

@JvmInline
private value class Rgb(val value: Int) {
    val r: UByte
        get() = ((value and 0xff0000) shr 16).toUByte()
    val g: UByte
        get() = ((value and 0xff00) shr 8).toUByte()
    val b: UByte
        get() = (value and 0xff).toUByte()

    companion object {
        fun parse(input: String): Rgb {
            require(input.first() == '#')
            return Rgb(input.drop(1).toInt(16))
        }
    }
}

private data class PlanItem(val direction: Dir, val length: Int, val colour: Rgb)

private fun parse(input: String): List<PlanItem> = input.lineSequence().map { line ->
    val (dStr, numStr, colStr) = line.split(' ')
    val dir = when (dStr) {
        "U" -> Dir.Up
        "R" -> Dir.Right
        "D" -> Dir.Down
        "L" -> Dir.Left
        else -> throw IllegalArgumentException(dStr)
    }
    PlanItem(dir, numStr.toInt(), colStr.drop(1).dropLast(1).let(Rgb::parse))
}.toList()

private data class Hole(val colour: Rgb?)

private class Board {
    private val holes: MutableMap<Coord, Hole> = mutableMapOf()
    var rows: IntRange = IntRange.EMPTY
        private set

    var cols: IntRange = IntRange.EMPTY
        private set

    private fun IntRange.extend(value: Int): IntRange = if (value in this) {
        this
    } else {
        if (value < start) {
            value..endInclusive
        } else {
            check(value > endInclusive)
            start..value
        }
    }

    operator fun get(coord: Coord): Hole? = holes[coord]

    operator fun contains(coord: Coord): Boolean = coord in holes

    operator fun set(coord: Coord, value: Hole) {
        rows = rows.extend(coord.row)
        cols = cols.extend(coord.col)
        holes[coord] = value
    }

    val numHoles: Int
        get() = holes.size

    fun printPretty(curPos: Coord? = null) {
        rows.forEach { row ->
            cols.forEach { col ->
                val coord = Coord(row, col)
                val v = get(coord)
                if (v == null) {
                    if (curPos == coord) {
                        print('x')
                    } else {
                        print('.')
                    }
                } else {
                    if (curPos == coord) {
                        v.colour.colourPrint('X')
                    } else {
                        v.colour.colourPrint('#')
                    }

                }
            }
            println()
        }
    }
}

private fun (Rgb?).colourPrint(char: Char) {
    if (this == null) {
        print(char)
    } else {
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
}

fun main() = aoc(2023, 18, ::parse) {
    example(
        """
        R 6 (#70c710)
        D 5 (#0dc571)
        L 2 (#5713f0)
        D 2 (#d2c081)
        R 2 (#59c680)
        D 2 (#411b91)
        L 5 (#8ceee2)
        U 2 (#caa173)
        L 1 (#1b58a2)
        U 2 (#caa171)
        R 2 (#7807d2)
        U 3 (#a77fa3)
        L 2 (#015232)
        U 2 (#7a21e3)
    """.trimIndent()
    )

    part1 { items ->
        val board = Board()
        var pos = Coord(0, 0)
        board[pos] = Hole(null)
        items.forEach { item ->
            repeat(item.length) {
                pos += item.direction
                board[pos] = Hole(item.colour)
            }
        }

        // now do a flood fill - unlike the pipeline, we don't need to consider pipes/edges touching (I think)
        // we need to find a spot "inside" the perimeter to start with, I _think_ because of our auto-expanding board, the second row must always have a vertical wall which must separate the "inside" and "outside".
        // Certainly my input does, so we'll go with it.

        val startPoint = run {
            val row = board.rows.take(2).last()
            for (col in board.cols) {
                val coord = Coord(row, col)
                if (board[coord] != null) {
                    return@run Coord(row, col + 1)
                }
            }
            throw IllegalStateException()
        }

        val queue = ArrayDeque<Coord>()
        queue.offer(startPoint)

        fun Coord.neighbours(): Sequence<Coord> = (-1..1).asSequence().flatMap { r ->
            (-1..1).asSequence().map { c -> r to c }
        }.filterNot { (r, c) -> r == 0 && c == 0 }.map { (r, c) -> Coord(row + r, col + c) }

        while (queue.isNotEmpty()) {
            val cur = queue.poll()
            if (cur in board) continue
            board[cur] = Hole(null)
            cur.neighbours().filterNot { it in board }.forEach { queue.offer(it) }
        }

        board.numHoles
    }
}
