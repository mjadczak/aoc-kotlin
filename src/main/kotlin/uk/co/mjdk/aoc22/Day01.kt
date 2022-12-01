package uk.co.mjdk.aoc22

import uk.co.mjdk.aoc.*

fun main() {
    // Part 1
    aocInput(22, 1).useLines { lines ->
        lines
            .splitBy { line -> line.isEmpty() }
            .map { strs ->
                strs.sumOf { Integer.parseInt(it) }
            }
            .max()
            .let { println(it) }
    }

    // Part 2
    aocInput(22, 1).useLines { lines ->
        lines
            .splitBy { line -> line.isEmpty() }
            .map { strs ->
                strs.sumOf { Integer.parseInt(it) }
            }
            .sortedDescending()
            .take(3)
            .sum()
            .let { println(it) }
    }
}
