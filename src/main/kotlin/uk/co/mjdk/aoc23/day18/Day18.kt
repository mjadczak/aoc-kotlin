package uk.co.mjdk.aoc23.day18

import uk.co.mjdk.aoc.aoc
import java.util.ArrayDeque
import kotlin.math.absoluteValue

private enum class Dir {
    Up,
    Right,
    Down,
    Left
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = moved(dir, 1)

    fun moved(dir: Dir, length: Int): Coord = when (dir) {
        Dir.Up -> copy(row = row - length)
        Dir.Down -> copy(row = row + length)
        Dir.Right -> copy(col = col + length)
        Dir.Left -> copy(col = col - length)
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

    val direction: Dir
        get() = when (val v = value and 0xf) {
            0 -> Dir.Right
            1 -> Dir.Down
            2 -> Dir.Left
            3 -> Dir.Up
            else -> throw IllegalArgumentException("$v not a dir (${value.toString(16)})")
        }

    val length: Int
        get() = value shr 4

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

    part2 { items ->
        // Essentially the shoelace formula, but simplified given we're axis-aligned.
        // We count (wlog) horizontal edges, and add up the signed area of the rectangles from that horizontal edge
        // to the bottom.
        // Our "bottom" and "top" are likely either side of the 0 line, but the maths should still work out fine given
        // we're dealing with the signed area.

        // Also, we need to deal with the fact that our coordinates are not lines with no area, but edges of a shape
        // - so we want our "vertex coordinates" to actually trace out an outline around the edges of the shape.
        // To do this, we keep track of each corner - whether it's "right" (outside) or "left" (inside).
        // Then the distance between each vertex increases by 1 when the edge is R -> R, decreases by 1 when it is L -> L,
        // and stays the same otherwise.

        // Otherwise we capture all the "bottom" and "left" edges but not the "top" and "right" ones.

        // An alternative method which I've actually got working is to do the calculation without this correction (thus
        // considering the area enclosed by lines through the _midpoints_ of the holes) and then correct for the missing
        // area around the perimeter (1/2 square per perimeter square). This then gets tricky around the corners, but it turns
        // out that this always just results in a missing 1 unit of area. This then turns out to just be an application of
        // Pick's theorem.

        // Above 0, Right -> Left adds area, and Left -> Right subtracts

        val initial = Coord(0, 0)

        val vertices = items.asSequence().runningFold(initial) { coord, item ->
            coord.moved(item.colour.direction, item.colour.length)
        } + initial

        val area = vertices.zipWithNext { v1, v2 ->
            when {
                v1.col == v2.col -> 0L // vertical edge, no change
                v1.row == v2.row -> (v1.col.toLong() - v2.col) * v1.row.toLong() // horizontal edge
                else -> throw IllegalArgumentException("Non axis-aligned: $v1 $v2")
            }
        }.sum().absoluteValue

        val perimeter = items.sumOf { it.colour.length.toLong() }
        check(perimeter % 2 == 0L)


        area + perimeter / 2 + 1
    }
}
