package uk.co.mjdk.aoc23.day13

import uk.co.mjdk.aoc.aoc

private enum class Cell {
    Ash,
    Rock;

    fun other(): Cell = when (this) {
        Ash -> Rock
        Rock -> Ash
    }
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

    fun columns(): List<List<Cell>> = cells[0].indices.map { col -> cells.map { it[col] } }

    override fun toString(): String = buildString {
        cells.forEach { row ->
            row.forEach { cell ->
                when (cell) {
                    Cell.Ash -> '.'
                    Cell.Rock -> '#'
                }.let { append(it) }
            }
            appendLine()
        }
    }
}

private fun parse(input: String): List<Board> = input.split("\n\n").map { Board(it) }

private fun reflectionOffset(list: List<List<Cell>>, except: Int? = null): Int? {
    // reflection can only start where we have two adjacent identical rows/columns
    return list.asSequence().zipWithNext().withIndex().filter { it.value.first == it.value.second }.map { it.index }
        .filter { startIdx ->
            val backwards = list.subList(0, startIdx + 1).asReversed()
            val forwards = list.subList(startIdx + 1, list.size)
            backwards.asSequence().zip(forwards.asSequence()).all { (l, r) -> l == r }
        }.map { it + 1 }.filterNot { it == except }.firstOrNull()
}

// One obvious way to make this faster is to start using BitSets or equivalent here instead
private fun <T> List<T>.singleChangeIndexToEqual(other: List<T>): Int? {
    var diff: Int? = null
    check(size == other.size)
    for (idx in indices) {
        if (get(idx) != other[idx]) {
            if (diff == null) {
                diff = idx
            } else {
                return null
            }
        }
    }
    return diff
}

// We _must_ make a single change, so in theory consider all combinations with one element changed
// We know there is only one solution, so as soon as we find one we can stop
// For a change to be plausible, the current (wlog) row must be exactly 1 char different from some subsequent row
// If this holds, make the flip and check the solution - again, once we find one we can stop
private fun reflectionOffsetWithOneChange(list: List<List<Cell>>): Int? {
    // lots of allocation here. Could get smarter with a mutable list or persistent collections, in practice almost certainly fine
    val plausibleBoards = run {
        val plausibleIndices = list.asSequence().flatMapIndexed { idx, row ->
            list.asSequence().drop(idx + 1)
                .mapNotNull { candRow -> row.singleChangeIndexToEqual(candRow)?.let { changeIdx -> idx to changeIdx } }
        }

        plausibleIndices.map { (idx, changeIdx) ->
            list.toMutableList().also { rows ->
                rows[idx] = rows[idx].toMutableList().also { cells ->
                    cells[changeIdx] = cells[changeIdx].other()
                }
            }.toList()
        }
    }

    val original = reflectionOffset(list)

    return plausibleBoards.mapNotNull { reflectionOffset(it, except = original) }.firstOrNull()
}

fun main() = aoc(2023, 13, ::parse) {
    part1 { boards ->
        boards.sumOf { board ->
            reflectionOffset(board.columns()) ?: reflectionOffset(board.rows())?.let { it * 100 }
            ?: throw IllegalStateException("\n$board")
        }
    }

    part2 { boards ->
        boards.sumOf { board ->
            reflectionOffsetWithOneChange(board.columns())
                ?: reflectionOffsetWithOneChange(board.rows())?.let { it * 100 }
                ?: throw IllegalStateException("\n$board")
        }
    }
}
