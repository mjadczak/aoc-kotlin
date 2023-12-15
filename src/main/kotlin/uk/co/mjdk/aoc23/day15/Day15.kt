package uk.co.mjdk.aoc23.day15

import uk.co.mjdk.aoc.aoc

private fun String.hash(): Int = fold(0) { acc, char ->
    @Suppress("ComplexRedundantLet")
    acc
        .let { it + char.code }
        .let { it * 17 }
        .let { it % 256 }
}

fun main() = aoc(2023, 15) {
    part1 { input ->
        input.splitToSequence(',').sumOf { it.hash() }
    }
}
