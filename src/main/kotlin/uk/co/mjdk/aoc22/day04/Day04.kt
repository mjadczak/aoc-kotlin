package uk.co.mjdk.aoc22.day04

import uk.co.mjdk.aoc.aocInput

val linePat = Regex("""(\d+)-(\d+),(\d+)-(\d+)""")

fun IntRange.contains(other: IntRange): Boolean {
    if (this.first > other.first) {
        return false
    }
    if (this.last < other.last) {
        return false
    }

    return true
}

fun main() {
    aocInput(22, 4).useLines { lines ->
        lines
            .map { line ->
                val res = linePat.matchEntire(line) ?: throw IllegalStateException()
                val (from1, to1, from2, to2) = res.groupValues.asSequence().drop(1).map(Integer::parseInt).toList()
                (from1..to1) to (from2..to2)
            }
            .filter { (r1, r2) -> r1.contains(r2) || r2.contains(r1) }
            .count()
            .let(::println)
    }
}
