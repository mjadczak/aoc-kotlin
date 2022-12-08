package uk.co.mjdk.aoc22.day06

import uk.co.mjdk.aoc.aocInput

fun main() {
    val input = aocInput(22, 6).use { it.readText() }.trim()
    val first = input.asSequence().windowed(4).withIndex().first {
        it.value.toSet().size == 4
    }
    println(first.index + 4)
}
