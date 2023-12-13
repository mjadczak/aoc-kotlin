package uk.co.mjdk.aoc23.day13

import uk.co.mjdk.aoc.aoc

private enum class Cell {
    Ash,
    Rock,
}

private class Board(input: String) {
    private val cells: Array<Array<Cell>>

    init {
        val lines = input.lines()
        cells = Array(lines.size) { row ->
            Array(lines[row].length) { col ->
                when (lines[row][col]) {
                    '.' -> Cell.Ash
                    '#' -> Cell.Rock
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    // could construct some sort of virtual custom List, but we may not need to
    fun rows(): List<List<Cell>> = cells.map { it.toList() }

    fun columns(): List<List<Cell>> = (0..<cells[0].size).map { col -> cells.map { it[col] } }
}

private fun parse(input: String): List<Board> = input.split("\n\n").map { Board(it) }

private fun reflectionOffset(list: List<List<Cell>>): Int? {
    // reflection can only start where we have two adjacent identical rows/columns
    return list.asSequence().zipWithNext().withIndex().filter { it.value.first == it.value.second }.map { it.index }
        .filter { startIdx ->
            val backwards = list.subList(0, startIdx + 1).asReversed()
            val forwards = list.subList(startIdx + 1, list.size)
            backwards.asSequence().zip(forwards.asSequence()).all { (l, r) -> l == r }
        }.map { it + 1 }.firstOrNull()
}

fun main() = aoc(2023, 13, ::parse) {
    part1 { boards ->
        boards.sumOf { board ->
            reflectionOffset(board.columns()) ?: reflectionOffset(board.rows())?.let { it * 100 }
            ?: throw IllegalStateException()
        }
    }
}
