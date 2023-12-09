package uk.co.mjdk.aoc23.day09

import uk.co.mjdk.aoc.aoc

private fun parse(input: String): List<LongArray> =
    input.lines().map { line ->
        val strs = line.split(Regex("""\s+"""))
        LongArray(strs.size) { strs[it].toLong() }
    }


fun main() = aoc(2023, 9, ::parse) {
    fun LongArray.diffs(): LongArray {
        require(this.size >= 2)
        return LongArray(this.size - 1) { idx -> this[idx + 1] - this[idx] }
    }

    fun LongArray.nextNum(): Long =
        if (this.all { it == 0L }) {
            0
        } else {
            this.last() + this.diffs().nextNum()
        }

    fun LongArray.prevNum(): Long =
        if (this.all { it == 0L }) {
            0
        } else {
            this.first() - this.diffs().prevNum()
        }

    part1 { arys ->
        arys.sumOf { it.nextNum() }
    }

    part2 { arys ->
        arys.sumOf { it.prevNum() }
    }
}
