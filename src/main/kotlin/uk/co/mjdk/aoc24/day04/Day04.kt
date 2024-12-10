package uk.co.mjdk.aoc24.day04

import uk.co.mjdk.aoc.aoc
import java.util.Objects
import kotlin.math.max
import kotlin.math.min

// in the general case we might want to use something like Aho-Corasick instead, but not needed for this

fun countXmasOccurrences(cs: CharSequence): Int {
    var count = 0
    cs.forEachIndexed { idx, char ->
        if (char == 'X') {
            if (idx + 4 <= cs.length && cs.subSequence(idx, idx + 4) == "XMAS") {
                count += 1
            }
            if (idx >= 3 && cs.subSequence(idx - 3, idx + 1) == "SAMX") {
                count += 1
            }
        }
    }
    return count
}

// returns the indexes with the middle A
fun findMasOccurrences(cs: CharSequence): List<Int> =
    buildList {
        cs.forEachIndexed { idx, char ->
            if (char == 'M') {
                if (idx + 3 <= cs.length && cs.subSequence(idx, idx + 3) == "MAS") {
                    add(idx + 1)
                }
                if (idx >= 2 && cs.subSequence(idx - 2, idx + 1) == "SAM") {
                    add(idx - 1)
                }
            }
        }
    }

class CharSubSequence(val cs: CharSequence, val start: Int, val end: Int) : CustomCharSequence() {
    override val length: Int = end - start
    override fun get(index: Int): Char = cs[start + index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSubSequence =
        CharSubSequence(cs, start + startIndex, start + endIndex)

    override fun toString(): String = String(CharArray(length) { get(it) })

    init {
        require(start <= end)
        require(cs.length >= length)
    }
}

data class Coord(val row: Int, val col: Int)

abstract class CustomCharSequence : CharSequence {
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = CharSubSequence(this, startIndex, endIndex)
    override fun toString(): String = String(CharArray(length) { get(it) })
    override fun equals(other: Any?): Boolean {
        if (other is CharSequence) {
            return length == other.length && (0..<length).all { i -> get(i) == other.get(i) }
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return Objects.hash(toString())
    }
}

class Board(private val lines: Array<CharArray>) {
    val rows = lines.size
    val cols = lines.first().size

    val horizontal: Sequence<CharSequence>
        get() = sequence {
            (0..<rows).forEach { row ->
                yield(object : CustomCharSequence() {
                    val rowData = lines[row]
                    override val length: Int
                        get() = rowData.size

                    override fun get(index: Int): Char = rowData[index]
                })
            }
        }

    val vertical: Sequence<CharSequence>
        get() = sequence {
            (0..<cols).forEach { col ->
                yield(object : CustomCharSequence() {
                    override val length: Int
                        get() = rows

                    override fun get(index: Int): Char = lines[index][col]
                })
            }
        }

    inner class DiagCharSequence(maxDiagLen: Int, squareLen: Int, diagNum: Int, val topLeft: Boolean) :
        CustomCharSequence() {
        val diagLen = min(squareLen, maxDiagLen)
        val startRow = min(diagNum, rows - 1)
        val startCol = if (topLeft) max(0, diagNum - (rows - 1)) else min(cols - 1, cols - 1 - (diagNum - (rows - 1)))

        override val length: Int
            get() = diagLen

        override fun get(index: Int): Char {
            val coord = coordOfIndex(index)
            return lines[coord.row][coord.col]
        }

        fun coordOfIndex(index: Int): Coord {
            val colIdx = if (topLeft) startCol + index else startCol - index
            return Coord(startRow - index, colIdx)
        }
    }

    private fun diagSequence(topLeft: Boolean): Sequence<DiagCharSequence> = sequence {
        val numDiags = rows + cols - 1
        val maxDiagLen = min(rows, cols)
        var squareLen = 1
        (0..<numDiags).forEach { diagNum ->
            yield(DiagCharSequence(maxDiagLen, squareLen, diagNum, topLeft))
            if (diagNum < numDiags / 2) {
                squareLen += 1
            } else {
                squareLen -= 1
            }
        }
    }

    val diag1: Sequence<DiagCharSequence>
        get() = diagSequence(true)

    val diag2: Sequence<DiagCharSequence>
        get() = diagSequence(false)
}

fun main() = aoc(2024, 4, { input -> Board(input.lines().map { it.toCharArray() }.toTypedArray()) }) {
    part1 { board ->
        listOf(
            board.horizontal,
            board.vertical,
            board.diag1,
            board.diag2,
        ).sumOf { seqs -> seqs.sumOf { countXmasOccurrences(it) } }
    }

    part2 { board ->
        val leftCoords = board.diag1.flatMap { diag ->
            findMasOccurrences(diag).map { diag.coordOfIndex(it) }
        }.toSet()
        val rightCoords = board.diag2.flatMap { diag ->
            findMasOccurrences(diag).map { diag.coordOfIndex(it) }
        }.toSet()
        leftCoords.intersect(rightCoords).size
    }
}
