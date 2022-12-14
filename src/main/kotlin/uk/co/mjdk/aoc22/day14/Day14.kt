package uk.co.mjdk.aoc22.day14

import uk.co.mjdk.aoc.aocInput
import kotlin.math.max
import kotlin.math.min

data class Pos(val x: Int, val y: Int) {
    fun fallCandidates(): List<Pos> = listOf(copy(y = y + 1), copy(y = y + 1, x = x - 1), copy(y = y + 1, x = x + 1))

    operator fun rangeTo(other: Pos): Sequence<Pos> {
        return if (x == other.x) {
            (min(y, other.y)..max(y, other.y)).asSequence().map { Pos(x, it) }
        } else {
            assert(y == other.y)
            (min(x, other.x)..max(x, other.x)).asSequence().map { Pos(it, y) }
        }
    }
}

enum class CellContents {
    Nothing,
    Rock,
    Sand;

    fun isEmpty(): Boolean = this == Nothing
}

// Lazy day today: manually hardcode table sizes, forgo range optimisation stuff

fun main() {
    aocInput(22, 14).useLines { lines ->
        val grid = List(600) { MutableList(200) { CellContents.Nothing } }
        fun cellAt(pos: Pos): CellContents = grid[pos.x][pos.y]
        fun setAt(pos: Pos, contents: CellContents) {
            grid[pos.x][pos.y] = contents
        }

        lines.forEach { line ->
            val tuples = line.split(" -> ").map {
                val (x, y) = it.split(",")
                Pos(x.toInt(), y.toInt())
            }
            assert(tuples.size >= 2)
            tuples.asSequence().windowed(2).forEach { (from, to) ->
                (from..to).forEach {
                    setAt(it, CellContents.Rock)
                }
            }
        }

        val sandStart = Pos(500, 0)
        var sandCounter = 0
        outer@ while (true) {
            var sandPos = sandStart
            while (true) {
                val nextPos = sandPos.fallCandidates().firstOrNull { cellAt(it).isEmpty() }
                if (nextPos == null) {
                    setAt(sandPos, CellContents.Sand)
                    break
                }
                if (nextPos.y == grid[0].size - 1) {
                    // we're at the bottom of the screen
                    break@outer
                }
                sandPos = nextPos
            }
            sandCounter += 1
        }
        println(sandCounter)
    }
}
