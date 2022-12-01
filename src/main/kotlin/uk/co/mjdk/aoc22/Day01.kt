package uk.co.mjdk.aoc22

import uk.co.mjdk.aoc.*

fun main() {
    // Part 1
    aocInput(22, 1).useLines { lines ->
        lines
            .splitBy(String::isEmpty)
            .map { it.sumOf(Integer::parseInt) }
            .max()
            .let(::println)
    }

    // Part 2
    aocInput(22, 1).useLines { lines ->
        lines
            .splitBy(String::isEmpty)
            .map { it.sumOf(Integer::parseInt) }
            .sortedDescending()
            .take(3)
            .sum()
            .let(::println)
    }
}
