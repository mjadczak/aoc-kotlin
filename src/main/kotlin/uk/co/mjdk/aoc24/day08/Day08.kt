package uk.co.mjdk.aoc24.day08

import uk.co.mjdk.aoc.aoc

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

data class Coord(val row: Int, val col: Int) {
    operator fun plus(other: Coord) = Coord(row + other.row, col + other.col)
    operator fun minus(other: Coord) = Coord(row - other.row, col - other.col)

    fun simplify(): Coord = gcd(row, col).let { d -> Coord(row / d, col / d) }
}

data class Antenna(val frequency: Char, val coord: Coord)

data class Board(val rows: Int, val cols: Int, val antennae: Set<Antenna>) {
    val antennaeByFrequency = antennae.groupBy { it.frequency }

    operator fun contains(coord: Coord): Boolean = coord.row in 0..<rows && coord.col in 0..<cols

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val antennae = buildSet {
                lines.forEachIndexed { row, line ->
                    line.forEachIndexed { col, char ->
                        if (char != '.') {
                            add(Antenna(char, Coord(row, col)))
                        }
                    }
                }
            }
            return Board(lines.size, lines[0].length, antennae)
        }
    }
}

fun main() = aoc(2024, 8, Board::parse) {
    part1 { board ->
        val antinodeCoords = buildSet {
            board.antennaeByFrequency.values.forEach { antennae ->
                antennae.forEach { a1 ->
                    antennae.forEach { a2 ->
                        if (a1 != a2) {
                            val delta = a2.coord - a1.coord
                            add(a2.coord + delta)
                        }
                    }
                }
            }
        }
        antinodeCoords.filter { it in board }.size
    }

    part2 { board ->
        val antinodeCoords = buildSet {
            board.antennaeByFrequency.values.forEach { antennae ->
                antennae.forEach { a1 ->
                    antennae.forEach { a2 ->
                        if (a1 != a2) {
                            val delta = (a2.coord - a1.coord).simplify()
                            // we're doing the work twice, but meh
                            generateSequence(a1.coord) { it - delta }.takeWhile { it in board }.forEach { add(it) }
                            generateSequence(a1.coord) { it + delta }.takeWhile { it in board }.forEach { add(it) }
                        }
                    }
                }
            }
        }
        antinodeCoords.filter { it in board }.size
    }
}
