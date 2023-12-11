package uk.co.mjdk.aoc23.day11

import uk.co.mjdk.aoc.aoc
import kotlin.math.max
import kotlin.math.min

private data class Coord(val row: Long, val col: Long)

private data class Board(val rows: Long, val cols: Long, val galaxies: List<Coord>) {
    val emptyRows: Set<Long> = (0..<rows).subtract(galaxies.map { it.row }.toSet())
    val emptyCols: Set<Long> = (0..<cols).subtract(galaxies.map { it.col }.toSet())

    fun manhattan(from: Coord, to: Coord, big: Boolean = false): Long {
        val mult = if (big) 1_000_000L else 2L
        fun dist(c1: Long, c2: Long, extras: Set<Long>): Long {
            val cMin = min(c1, c2)
            val cMax = max(c1, c2)
            return cMax - cMin + ((cMin..cMax).intersect(extras).count() * (mult - 1))
        }
        return dist(from.row, to.row, emptyRows) + dist(from.col, to.col, emptyCols)
    }

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val galaxies = lines.flatMapIndexed { row, line ->
                line.mapIndexedNotNull { col, c ->
                    when (c) {
                        '.' -> null
                        '#' -> Coord(row.toLong(), col.toLong())
                        else -> throw IllegalArgumentException(c.toString())
                    }
                }
            }
            return Board(lines.size.toLong(), lines[0].length.toLong(), galaxies)
        }
    }
}

private fun <T> List<T>.allPairs(): List<Pair<T, T>> = flatMapIndexed { idx, fst ->
    drop(idx + 1).map { snd -> fst to snd }
}

fun main() = aoc(2023, 11, Board::parse) {
    part1 { board ->
        board.galaxies.allPairs().sumOf { (g1, g2) -> board.manhattan(g1, g2) }
    }

    part2 { board ->
        board.galaxies.allPairs().sumOf { (g1, g2) -> board.manhattan(g1, g2, big = true) }
    }
}
