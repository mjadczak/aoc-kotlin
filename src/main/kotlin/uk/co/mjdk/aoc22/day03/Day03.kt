package uk.co.mjdk.aoc22.day03

import uk.co.mjdk.aoc.aocReader

fun main() {
    // Part 1
    aocReader(22, 3).useLines { lines ->
        lines
            .map { line ->
                val length = line.length
                assert(length % 2 == 0)
                val left = line.take(length / 2).toSet()
                val right = line.drop(length / 2).toSet()
                val common = left.intersect(right)
                assert(common.count() == 1)
                common.first()
            }
            .map {
                when (it) {
                    in 'a'..'z' -> it - 'a' + 1
                    in 'A'..'Z' -> it - 'A' + 27
                    else -> throw IllegalStateException()
                }
            }
            .sum()
            .let(::println)
    }

    // Part 2
    aocReader(22, 3).useLines { lines ->
        lines
            .map { it.toSet() }
            .chunked(3)
            .map { it.reduce { s1, s2 -> s1.intersect(s2) } }
            .map {
                assert(it.count() == 1)
                it.first()
            }
            .map {
                when (it) {
                    in 'a'..'z' -> it - 'a' + 1
                    in 'A'..'Z' -> it - 'A' + 27
                    else -> throw IllegalStateException()
                }
            }
            .sum()
            .let(::println)
    }
}
