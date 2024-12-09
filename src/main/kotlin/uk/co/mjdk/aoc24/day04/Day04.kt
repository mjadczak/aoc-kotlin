package uk.co.mjdk.aoc24.day04

import uk.co.mjdk.aoc.aoc

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

data class Board(private val lines: Array<CharArray>) {
    val rows = lines.size
    val cols = lines.first().size

    val horizontal: Sequence<CharSequence>
        get() = sequence {
            (0..<rows).forEach { row ->
                yield(object : CharSequence {
                    val rowData = lines[row]
                    override val length: Int
                        get() = rowData.size

                    override fun get(index: Int): Char = rowData[index]

                    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = rowData.subSequence()
                })
            }
        }
}

fun main() = aoc(2024, 4, {input -> Board(input.lines().map { it.toCharArray() }.toTypedArray())}) {

}
