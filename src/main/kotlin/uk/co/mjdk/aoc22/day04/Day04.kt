package uk.co.mjdk.aoc22.day04

import uk.co.mjdk.aoc.aocReader

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
    // Part 1
    aocReader(22, 4).useLines { lines ->
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

    // Part 2
    aocReader(22, 4).useLines { lines ->
        lines
            .map { line ->
                val res = linePat.matchEntire(line) ?: throw IllegalStateException()
                val (from1, to1, from2, to2) = res.groupValues.asSequence().drop(1).map(Integer::parseInt).toList()
                (from1..to1) to (from2..to2)
            }
            .filter { (r1, r2) ->
                r1.contains(r2.first) || r1.contains(r2.last) || r2.contains(r1.first) || r2.contains(
                    r1.last
                )
            }
            .count()
            .let(::println)
    }
}
