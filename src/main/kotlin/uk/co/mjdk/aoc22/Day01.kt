package uk.co.mjdk.aoc22

import uk.co.mjdk.aoc.*

fun useCalorieSums(receiver: (Sequence<Int>) -> Unit) {
    aocInput(22, 1).useLines { lines ->
        lines
            .splitBy(String::isEmpty)
            .map { it.sumOf(Integer::parseInt) }
            .let(receiver)
    }
}

fun main() {
    // Part 1
    useCalorieSums { it.max().let(::println) }

    // Part 2
    useCalorieSums { it.sortedDescending().take(3).sum().let(::println) }
}
